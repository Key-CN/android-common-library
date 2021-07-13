package io.keyss.library.common.network

/**
 * @author Key
 * Time: 2021/07/09 11:36
 * Description: 网卡信息，只保留IPv4
 */
data class InterfaceConfig(
    val name: String,
    var addr: String = "",
    var mask: String = "",
    var bcast: String = "",
)
