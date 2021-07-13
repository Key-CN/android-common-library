package io.keyss.library.common.ding

/**
 * @author Key
 * Time: 2020/03/30 15:04
 * Description:
 */

data class TextMessage(val text: Text) : DingMessage {
    var at: At? = null
    val msgtype: String = "text"

    constructor(message: String, vararg ats: String) : this(Text(message)) {
        if (!ats.isNullOrEmpty()) {
            at = At(ats.asList())
        }
    }

    constructor(message: String, ats: List<String>?) : this(Text(message)) {
        if (!ats.isNullOrEmpty()) {
            at = At(ats)
        }
    }
}