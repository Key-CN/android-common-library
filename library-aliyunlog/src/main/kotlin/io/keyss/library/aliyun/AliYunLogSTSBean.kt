package io.keyss.library.aliyun

data class AliYunLogSTSBean(
    var accessKeyId: String,
    var accessKeySecret: String,
    var securityToken: String? = null,
    var expiration: String? = null,
)