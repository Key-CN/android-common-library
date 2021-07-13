package io.keyss.library.common.network

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.ContextCompat
import io.keyss.library.common.utils.ShellUtil
import java.net.Inet4Address
import java.net.NetworkInterface


/**
 * @author Key
 * Time: 2021/07/07 14:08
 * Description:
 */
object NetworkUtil {

    data class Ping(
        val originalText: String,
        var packetLossRate: String? = null,
        var min: String? = null,
        var avg: String? = null,
        var max: String? = null,
        var mdev: String? = null,
    ) {
        fun isUnreachable(): Boolean {
            return packetLossRate == null || packetLossRate == "100%"
        }
    }

    val ipRegex = "((2[0-4]\\d|25[0-5]|[01]?\\d\\d?)\\.){3}(2[0-4]\\d|25[0-5]|[01]?\\d\\d?)".toRegex()

    /**
     * 没有无符号类型，请手动指定参数在0以上
     * timeout可以带小数点，但是没有意义
     * 不通的总时长 = timeout + interval * (count - 1)
     */
    fun getPing(count: Int = 20, interval: Float = 0.2F, timeout: Int = 5, destination: String = "114.114.114.114"): Ping? {
        val shellResult = ShellUtil.executeShell("ping -c $count -i $interval -q -W $timeout $destination")
        //--- 114.114.114.114 ping statistics ---
        //20 packets transmitted, 20 received, 0% packet loss, time 3811ms
        //rtt min/avg/max/mdev = 21.565/22.606/25.142/0.792 ms
        //println(shellResult)
        if (shellResult.success) {
            val ping = Ping(shellResult.text)
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
        return null
    }

    /**
     * 获取路由网关IP地址
     */
    fun getDefaultGateway(): String? {
        val shellResult = ShellUtil.executeShell("/bin/sh", "-c", "ip route list table 0 | grep 'default via'")

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
                    if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                        /*println(
                            "${inetAddress::class} - name=${ni.name}, " +
                                    "isLoopback=${ni.isLoopback}, " +
                                    "isUp=${ni.isUp}, " +
                                    "isVirtual=${ni.isVirtual}, " +
                                    "supportsMulticast=${ni.supportsMulticast()}, " +
                                    "displayName=${ni.displayName}, " +
                                    "getIPAddress Inet4Address=$inetAddress, hostName=${inetAddress.hostName}, hostAddress=${inetAddress.hostAddress}"
                        )*/
                        list.add(InterfaceConfig(ni.name, inetAddress.hostAddress))
                        continue
                    }
                }
            }
        }
        return list
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