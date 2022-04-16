package io.keyss.library.common.network

/**
 * @author Key
 * @date 2021/07/09 11:36
 * @Description: 网卡信息，只保留IPv4
 */
data class InterfaceConfig(
    /** 接口名 */
    var name: String,
    /** 识别名，如Wi-Fi名 */
    var connectionName: String = "",
    /** IPv4 */
    var addr: String = "",
    /** 子网掩码 */
    var mask: String = "",
    /** 广播地址 */
    var bcast: String = "",
) {
    fun toHumanString(): String {
        return "网卡：${name}${if (connectionName.isBlank()) "" else "，连接名：${connectionName}"}，IP：$addr"
    }
}
