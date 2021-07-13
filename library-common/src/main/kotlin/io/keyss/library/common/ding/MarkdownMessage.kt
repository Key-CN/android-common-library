package io.keyss.library.common.ding


/**
 * @author Key
 * Time: 2019/11/26 12:42
 * Description:
 */
data class MarkdownMessage(var markdown: Markdown?) : DingMessage {
    var at: At? = null
    val msgtype: String = "markdown"

    constructor() : this(null)

    constructor(title: String, text: String, vararg ats: String) : this(Markdown(title, text)) {
        if (!ats.isNullOrEmpty()) {
            at = At(ats.asList())
        }
    }

    constructor(title: String, text: String, ats: List<String>?) : this(Markdown(title, text)) {
        if (!ats.isNullOrEmpty()) {
            at = At(ats)
        }
    }
}