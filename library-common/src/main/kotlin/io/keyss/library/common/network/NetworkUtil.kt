package io.keyss.library.common.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkAddress
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import io.keyss.library.common.extensions.toHexString
import io.keyss.library.common.utils.ShellUtil
import java.io.File
import java.net.*


/**
 * @author Key
 * Time: 2021/07/07 14:08
 * Description:
 */
object NetworkUtil {
    private const val TAG = "NetworkUtil"

    /**
     * IP address       HW type     Flags       HW address            Mask     Device
     */
    data class Arp(
        val IpAddress: String,
        val HwAddress: String,
        val Device: String,
    )

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
                    append("丢包率=${packetLossRate}, min=${min}ms, avg=${avg}ms, max=${max}ms, mdev=${mdev}ms")
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

    /**
     * 获取arp表
     */
    fun getArp(): MutableList<Arp>? {
        getDefaultGateway().takeIf { !it.isNullOrBlank() }?.dropLastWhile { it != '.' }?.let { gatewayPrefix ->
            val dp = DatagramPacket(ByteArray(0), 0, 0)
            val socket = DatagramSocket()
            for (i in 2 until 255) {
                //GlobalScope.launch(Dispatchers.IO) {
                val ip = gatewayPrefix + i
                //val reachable = InetAddress.getByName(ip).isReachable(1000)
                //println("ip: $ip, reachable=$reachable")
                dp.address = InetAddress.getByName(ip)
                socket.send(dp)
                //}
            }
            socket.close()
            //SystemClock.sleep(7_000)
            //val shellResult = ShellUtil.executeShell("cat proc/net/arp")
//            val readText = File("/proc/net/arp").readText()
//            println("getArp Result: $readText")
            val regex = "\\s+".toRegex()
            val list = mutableListOf<Arp>()
            File("/proc/net/arp").forEachLine { line ->
                if (line.contains("0x2")) {
                    line.split(regex).takeIf { it.size == 6 }?.let {
                        list.add(Arp(it[0], it[3], it[5]))
                    }
                }
            }
            println(list)
            return list
        }
        return null
    }

    fun getWifiInfo(context: Context): WifiInfo? {
        return ContextCompat.getSystemService(context, WifiManager::class.java)?.connectionInfo
    }

    /**
     * 没权限会是unknown
     */
    fun getWifiName(context: Context): String {
        return getWifiInfo(context)?.ssid?.removeSurrounding("\"", "\"") ?: "获取不到Wi-Fi名"
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
                        // 不为空才继续获取
                        ?.apply {
                            // 获取IP
                            getIPv4Address(linkProperties.linkAddresses)?.let {
                                addr = it
                            }
                            cm.getNetworkCapabilities(network)?.let { networkCapabilities ->
                                connectionName = when {
                                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> getWifiName(context)
                                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "以太网"
                                    else -> networkCapabilities.toString()
                                }
                            }
                            hardwareAddress = getHardwareAddress(name)
                        }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
        if (ifconfig == null) {
            (getInterfaceConfigByShell(context).takeIf { it.isNotEmpty() } ?: getInterfaceConfigByApi(context).takeIf { it.isNotEmpty() })?.let {
                if (it.size == 1) {
                    ifconfig = it[0]
                } else {
                    for (interfaceConfig in it) {
                        // TODO isEthernetFirst优先则直接返回，不优先则先记录一下，没有Wi-Fi才返回eth
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
    fun getInterfaceConfigByShell(
        context: Context? = null,
        ignoreEmptyIp: Boolean = true,
        ignoreLoopback: Boolean = true
    ): MutableList<InterfaceConfig> {
        val list: MutableList<InterfaceConfig> = mutableListOf<InterfaceConfig>()
        ShellUtil.executeShellStream("ifconfig", inputBlock = { br ->
            var tempInterface: InterfaceConfig? = null
            br.forEachLine { lineStr ->
                if (!lineStr.startsWith(" ") && lineStr.isNotBlank()) {
                    val name = lineStr.takeWhile { it != ' ' }
                    // 顺便 add mac
                    tempInterface = InterfaceConfig(name = name, hardwareAddress = getHardwareAddress(name))
                } else {
                    tempInterface?.let { ifc ->
                        when {
                            lineStr.isEmpty() -> {
                                // 一个信息块结束
                                // 接口名必须存在，IP不忽略或不为空
                                // 忽略localhost
                                if (ifc.name.isNotBlank() && (!ignoreEmptyIp || ifc.addr.isNotBlank()) && !(ignoreLoopback && (ifc.name == "lo" || ifc.addr == "127.0.0.1"))) {
                                    list.add(ifc)
                                }
                                tempInterface = null
                            }
                            lineStr.trim().startsWith("inet ") -> {
                                // inet addr:127.0.0.1  Mask:255.0.0.0
                                // inet addr:192.168.101.3  Bcast:192.168.101.255  Mask:255.255.255.0
                                // inet6 addr: fe80::346:d372:e7a7:f48a/64 Scope: Link
                                ifc.addr = lineStr.substringAfter("addr:").substringBefore(" ")
                                ifc.bcast = lineStr.substringAfter("Bcast:").substringBefore(" ")
                                ifc.mask = lineStr.substringAfter("Mask:").substringBefore(" ")
                            }
                            else -> {
                                // ignore
                            }
                        }

                    }
                }
            }
        })
        if (null != context) {
            putInWifiName(context, list)
        }
        return list
    }

    /**
     * 目前的网络状况直接使用IPv4
     * 没有IP就没有inetAddresses，会是空[]
     * 所以更推荐使用[getInterfaceConfigByShell]
     */
    fun getInterfaceConfigByApi(context: Context? = null): MutableList<InterfaceConfig> {
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
                        inetAddress.address
                        list.add(
                            InterfaceConfig(
                                name = ni.name,
                                addr = numericToTextFormat(inetAddress.address),
                                hardwareAddress = ni.hardwareAddress?.toHexString(":") ?: getHardwareAddress(ni.name)
                            )
                        )
                        continue
                    }
                }
            }
        }
        if (null != context) {
            putInWifiName(context, list)
        }
        return list
    }

    private fun putInWifiName(context: Context, list: List<InterfaceConfig>) {
        getWifiInfo(context)?.let {
            for (interfaceConfig in list) {
                // 主备不一定哪个是活跃，所以最好还是用活跃网卡做处理
                if (interfaceConfig.name.startsWith("wlan") && intToIPv4(it.ipAddress) == interfaceConfig.addr) {
                    interfaceConfig.connectionName = it.ssid.removeSurrounding("\"", "\"")
                    // 一般的Android不可能存在两个Wi-Fi，新款手机，类似小米，5G连了之后，2.4G可以连一个备用Wi-Fi,这样的话一般wlan0主，wlan1备
                    break
                }
            }
        }
    }

    /**
     * shell读取硬件地址的文件
     * 一般：/sys/class/net/eth0/address
     * /sys/class/net/wlan0
     * 可以ls /sys/class/net路径下查看机器所含接口
     * 不需要su权限可以读取，但是有些手机可能是不行的
     */
    fun getHardwareAddress(interfaceName: String): String {
        val executeResult = ShellUtil.executeShell("cat /sys/class/net/${interfaceName}/address")
        return if (executeResult.success) {
            Log.i(TAG, "getHardwareAddress: $interfaceName - ${executeResult.text}")
            executeResult.text.trimIndent()
        } else {
            Log.e(TAG, "获取Mac地址出错：${executeResult.text}")
            ""
        }
    }

    /**
     * 需要权限
     */
    fun getAllInterfaceList(): List<String> {
        val executeResult = ShellUtil.executeSuShell("ls /sys/class/net/")
        return if (executeResult.success) {
            Log.i(TAG, "getAllInterfaceList: ${executeResult.text}")
            executeResult.text.trimIndent().split("\\s+".toRegex())
        } else {
            Log.e(TAG, "获取网卡列表出错：${executeResult.text}")
            emptyList()
        }
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
    fun intToIPv4(ipInt: Int): String {
        val sb = StringBuilder()
        sb.append(ipInt and 0xFF).append(".")
        sb.append(ipInt shr 8 and 0xFF).append(".")
        sb.append(ipInt shr 16 and 0xFF).append(".")
        sb.append(ipInt shr 24 and 0xFF)
        return sb.toString()
    }

    /**
     * 参考Inet4Address类
     */
    fun numericToTextFormat(src: ByteArray?): String {
        if (null == src || src.size != 4) {
            return ""
        }
        return src.joinToString(".") { it.toUByte().toString() }
    }

    /**
     * 设置公共DNS
     */
    fun setPublicDNS() {
        ShellUtil.executeSuShell("setprop net.dns2 223.5.5.5", "setprop net.dns1 114.114.114.114")
    }

    /**
     * 测试外网是否连通
     *
     * @param url 使用域名测试会涉及到域名解析
     * @return
     */
    fun isInternetConnectivity(url: String = "http://180.97.34.94/", timeout: Int = 10_000): Boolean {
        return getConnectDelay(url, timeout) != null
    }

    /**
     * 测试真连接延迟
     */
    fun getConnectDelay(url: String = "http://180.97.34.94/", timeout: Int = 10_000): Long? {
        var delay: Long? = null
        try {
            val start = System.currentTimeMillis()
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.requestMethod = "HEAD"
            conn.connectTimeout = timeout
            conn.readTimeout = timeout
            conn.connect()
            delay = System.currentTimeMillis() - start
            conn.disconnect()
        } catch (e: Exception) {
            Log.w(TAG, "请求错误, url = [$url], error = ${e.message}", e)
        }
        return delay
    }
}