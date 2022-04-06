package io.keyss.library.common.utils

/**
 * @author Key
 * Time: 2022/04/06 19:32
 * Description:
 */
object SystemUtil {

    /**
     * 打开网络adb
     */
    @JvmStatic
    fun openNetworkADB(): ShellUtil.Result {
        return ShellUtil.executeSuShell(
            "setprop service.adb.tcp.port 5555",
            "stop adbd",
            "start adbd"
        )
    }
}