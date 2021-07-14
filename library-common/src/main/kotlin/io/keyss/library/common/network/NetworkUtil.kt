package io.keyss.library.common.network

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.LinkAddress
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import io.keyss.library.common.utils.ShellUtil
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface


/**
 * @author Key
 * Time: 2021/07/07 14:08
 * Description:
 */
object NetworkUtil {

    data class TestResult(
        var ip: MutableList<InterfaceConfig>? = null,
        var gateway: String? = null,
        var gatewayPing: Ping? = null,
        /** 默认使用了114 */
        var internetPing: Ping? = null,
        var specifiedDestination: String? = null,
        var specifiedPing: Ping? = null,
    ) {
        companion object {
            const val STEP0 = "无IP"
            const val STEP1 = "无网关"
            const val STEP2 = "ping不通网关"
            const val STEP3 = "ping不通114"
            const val STEP4 = "ping不通指定目的地"
            const val STEP5 = "通过"
            val STEP_DETAILS = arrayOf(STEP0, STEP1, STEP2, STEP3, STEP4, STEP5)
        }

        var step = 0

        fun getStepDetail(): String {
            if (step < 0 || step >= STEP_DETAILS.size) {
                return "获取详情错误"
            }
            return STEP_DETAILS[step]
        }
    }

    data class Ping(
        val destination: String,
        val originalText: String,
        var ip: String? = null,
        var packetLossRate: String? = null,
        var min: String? = null,
        var avg: String? = null,
        var max: String? = null,
        var mdev: String? = null
    ) {
        override fun toString(): String {
            return StringBuilder("${destination}(${ip}): ").apply {
                if (isUnreachable()) {
                    append(originalText)
                } else {
                    append("丢包率=${packetLossRate}, min=${min}, avg=${avg}, max=${max}, mdev=${mdev}")
                }
            }.toString()
        }

        fun isUnreachable(): Boolean {
            return packetLossRate == null || packetLossRate == "100%"
        }
    }

    val ipRegex = "((2[0-4]\\d|25[0-5]|[01]?\\d\\d?)\\.){3}(2[0-4]\\d|25[0-5]|[01]?\\d\\d?)".toRegex()


    /**
     * 整个链路的网络测试
     */
    suspend fun executeTesting(specifiedDestination: String? = null): TestResult {
        val startTimeMillis = System.currentTimeMillis()
        val testResult = TestResult()
        kotlin.run {
            // 1. 先看网络是否连接，是否有IP
            val interfaceConfigByShell = getInterfaceConfigByShell()
            if (interfaceConfigByShell.isEmpty()) {
                // 无IP,0
                println("无IP")
                return@run
            }
            testResult.ip = interfaceConfigByShell
            testResult.step++
            // 1

            val defaultGateway = getDefaultGateway()
            if (defaultGateway.isNullOrBlank()) {
                // 无网关,1
                println("无网关")
                return@run
            }
            testResult.gateway = defaultGateway
            testResult.step++
            // 2

            val ping = getPing(destination = defaultGateway)
            // ping不通的结果保留
            testResult.gatewayPing = ping
            if (ping == null || ping.isUnreachable()) {
                // ping不通网关,2
                println("ping不通网关")
                return@run
            }
            testResult.step++
            // 3

            val ping114 = getPing()
            // ping不通的结果保留
            testResult.internetPing = ping114
            if (ping114 == null || ping114.isUnreachable()) {
                // ping不通114,3
                println("ping不通114")
                return@run
            }
            testResult.step++
            // 4

            specifiedDestination?.takeIf { it.isNotBlank() }?.let {
                val pingSpecified = getPing(destination = it)
                // ping不通的结果保留
                testResult.specifiedDestination = specifiedDestination
                testResult.specifiedPing = pingSpecified
                if (pingSpecified == null || pingSpecified.isUnreachable()) {
                    // ping不通指定目的地,4
                    println("ping不通指定目的地, $pingSpecified")
                    return@run
                }
            }
            // 5
            testResult.step++
        }

        println("测试结束, 耗时: ${System.currentTimeMillis() - startTimeMillis}ms")
        return testResult
    }


    /**
     * 没有无符号类型，请手动指定参数在0以上
     * timeout可以带小数点，但是没有意义
     * 不通的总时长 = timeout + interval * (count - 1)
     */
    fun getPing(count: Int = 10, interval: Float = 0.2F, timeout: Int = 5, destination: String = "114.114.114.114"): Ping? {
        val shellResult = ShellUtil.executeShell("ping -c $count -i $interval -q -W $timeout $destination")
        //--- 114.114.114.114 ping statistics ---
        //20 packets transmitted, 20 received, 0% packet loss, time 3811ms
        //rtt min/avg/max/mdev = 21.565/22.606/25.142/0.792 ms
        //println(shellResult)
        if (shellResult.success) {
            val ping = Ping(destination, shellResult.text)
            ipRegex.find(ping.originalText)?.value?.takeIf { it.isNotBlank() }?.let {
                ping.ip = it
            }
            // connect: Network is unreachable
            if (ping.originalText.contains("packet loss", true)) {
                "(\\d+)%".toRegex().find(ping.originalText)?.let {
                    ping.packetLossRate = it.value
                }
                "((\\d+\\.\\d+)/){3}(\\d+\\.\\d+)".toRegex().find(ping.originalText)?.value?.split("/")?.takeIf { it.size == 4 }?.let {
                    ping.apply {
                        min = it[0]
                        avg = it[1]
                        max = it[2]
                        mdev = it[3]
                    }
                }
                return ping
            }
        }
        println("gePing执行错误结果：${shellResult}")
        return null
    }

    /**
     * 获取路由网关IP地址
     */
    fun getDefaultGateway(): String? {
        val shellResult = ShellUtil.executeShell("sh", "-c", "ip route list table 0 | grep 'default via'")

        return if (shellResult.success) {
            ipRegex.find(shellResult.text)?.value
        } else {
            println("getDefaultGateway Error Text: ${shellResult.text}")
            null
        }
    }

    fun getWifiInfo(context: Context): WifiInfo? {
        return ContextCompat.getSystemService(context, WifiManager::class.java)?.connectionInfo
    }

    /**
     * 获取当前活跃的网卡信息，双网卡是一般为eth
     */
    @RequiresApi(Build.VERSION_CODES.M)
    fun getActiveInterfaceConfig(context: Context, isEthernetFirst: Boolean = true): InterfaceConfig? {
        var ifconfig: InterfaceConfig? = null
        val cm = ContextCompat.getSystemService(context, ConnectivityManager::class.java)
        if (cm != null) {
            ifconfig = try {
                val network = cm.activeNetwork
                cm.getLinkProperties(network)?.let { linkProperties ->
                    // 获取接口名
                    linkProperties.interfaceName?.takeIf { it.isNotBlank() }?.let {
                        InterfaceConfig(it)
                    }
                        ?.apply {
                            // 获取IP
                            getIPv4Address(linkProperties.linkAddresses)?.let {
                                addr = it
                            }
                        }
                        ?.apply {
                            cm.getNetworkCapabilities(network)?.let { networkCapabilities ->
                                connectionName = when {
                                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                                        // 移除前后两个引号
                                        getWifiInfo(context)?.ssid?.removeSurrounding("\"", "\"") ?: "未获取到Wi-Fi名"
                                    }
                                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "以太网"
                                    else -> networkCapabilities.toString()
                                }
                            }
                        }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
        if (ifconfig == null) {
            (getInterfaceConfigByShell().takeIf { it.isNotEmpty() } ?: getInterfaceConfigByApi().takeIf { it.isNotEmpty() })?.let {
                if (it.size == 1) {
                    ifconfig = it[0]
                } else {
                    for (interfaceConfig in it) {
                        if (ifconfig == null || (isEthernetFirst && interfaceConfig.name.startsWith("eth"))) {
                            ifconfig = interfaceConfig
                        }
                    }
                }
            }
        }
        return ifconfig
    }

    /**
     * 采用shell的方式获取网卡信息
     */
    @JvmStatic
    fun getInterfaceConfigByShell(ignoreEmptyIp: Boolean = true, ignoreLoopback: Boolean = true): MutableList<InterfaceConfig> {
        val list: MutableList<InterfaceConfig> = mutableListOf<InterfaceConfig>()
        ShellUtil.executeShell("ifconfig", { br ->
            var tempInterface: InterfaceConfig? = null
            br.forEachLine { lineStr ->
                if (!lineStr.startsWith(" ") && lineStr.isNotBlank()) {
                    val name = lineStr.takeWhile { it != ' ' }
                    tempInterface = InterfaceConfig(name)
                } else {
                    tempInterface?.let {
                        when {
                            lineStr.isEmpty() -> {
                                // 一个信息块结束
                                // 接口名必须存在，IP不忽略或不为空
                                // 忽略localhost
                                if (it.name.isNotBlank() && (!ignoreEmptyIp || it.addr.isNotBlank()) && !(ignoreLoopback && (it.name == "lo" || it.addr == "127.0.0.1"))) {
                                    list.add(it)
                                }
                                tempInterface = null
                            }
                            lineStr.trim().startsWith("inet ") -> {
                                // inet addr:127.0.0.1  Mask:255.0.0.0
                                // inet addr:192.168.101.3  Bcast:192.168.101.255  Mask:255.255.255.0
                                it.addr = lineStr.substringAfter("addr:").substringBefore(" ")
                                it.bcast = lineStr.substringAfter("Bcast:").substringBefore(" ")
                                it.mask = lineStr.substringAfter("Mask:").substringBefore(" ")
                            }
                            else -> {
                                // ignore
                            }
                        }
                    }
                }
            }
        })
        return list
    }

    /**
     * 目前的网络状况直接使用IPv4
     * 没有IP就没有inetAddresses，会是空[]
     * 所以更推荐使用[getInterfaceConfigByShell]
     */
    fun getInterfaceConfigByApi(): MutableList<InterfaceConfig> {
        val list: MutableList<InterfaceConfig> = mutableListOf<InterfaceConfig>()
        try {
            // length = 0 会返回null
            NetworkInterface.getNetworkInterfaces()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }?.let { nif ->
            for (ni in nif) {
                if (!ni.isUp || ni.isLoopback) continue
                // Enumeration 无法判空
                for (inetAddress in ni.inetAddresses) {
                    if (inetAddress.isIPv4AndNotLoopback()) {
                        list.add(InterfaceConfig(ni.name, inetAddress.hostAddress))
                        continue
                    }
                }
            }
        }
        return list
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun getIPv4Address(addresses: List<LinkAddress>): String? {
        if (addresses.isNotEmpty()) {
            for (linkAddress in addresses) {
                linkAddress.address.let {
                    if (it.isIPv4AndNotLoopback()) {
                        return it.hostAddress
                    }
                }
            }
        }
        return null
    }

    /**
     * 非回路IPv4的地址
     */
    fun InetAddress.isIPv4AndNotLoopback(): Boolean {
        return !isLoopbackAddress && this.isIPv4()
    }

    fun InetAddress.isIPv4(): Boolean {
        return this is Inet4Address
    }

    @JvmStatic
    fun isGpsOpen(context: Context): Boolean {
        val locationManager = ContextCompat.getSystemService(context, LocationManager::class.java)
        return locationManager?.let {
            // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
            val gps = it.isProviderEnabled(LocationManager.GPS_PROVIDER)
            // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
            val agps = it.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            println("NetworkUtil isGpsOpen gps=${gps} agps=${agps}")
            gps || agps
        } ?: kotlin.run {
            println("NetworkUtil isGpsOpen LocationManager 获取失败")
            false
        }
    }

    /**
     * 打开GPS，请在主线程执行
     * 不try，加个toast非主线程直接蹦，方便看，必须开发期解决
     */
    @JvmStatic
    fun openGps(context: Context) {
        Toast.makeText(context, "请手动打开GPS开关", Toast.LENGTH_LONG).show()
        context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
    }

    @JvmStatic
    fun intToIPv4(ipInt: Int): String {
        val sb = StringBuilder()
        sb.append(ipInt and 0xFF).append(".")
        sb.append(ipInt shr 8 and 0xFF).append(".")
        sb.append(ipInt shr 16 and 0xFF).append(".")
        sb.append(ipInt shr 24 and 0xFF)
        return sb.toString()
    }
}