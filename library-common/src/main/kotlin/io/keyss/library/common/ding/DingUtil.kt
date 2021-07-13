package io.keyss.library.common.ding

import android.util.Base64
import java.net.URLEncoder
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * @author Key
 * Time: 2021/05/25 17:13
 * Description:
 */
object DingUtil {
    var WEBHOOK = ""
    var SECRET = ""
    private var mLastText: String = ""
    private var mLastTime: Long = 0
    var template = ""

    /**
     * 发送模版消息
     */
    fun sendTemplateMarkdown(title: String, message: String) {
        sendMarkdown(title, applyMarkdownTemplate(message))
    }

    /**
     * 套用模版
     */
    fun applyMarkdownTemplate(message: String): String = """
$template
$message
"""

    fun sendMarkdown(title: String, text: String, vararg ats: Linkman) {
        // 同一条消息一分钟内只发一次
        if (text == mLastText && System.currentTimeMillis() - mLastTime < 60 * 1_000) {
            return
        }
        mLastText = text
        mLastTime = System.currentTimeMillis()
        val arrayList = ArrayList<String>()
        var name = ""
        if (ats.isNotEmpty()) {
            name = "\n"
            ats.forEach {
                arrayList.add(it.phoneNumber)
                name += ("@${it.phoneNumber} ")
            }
        }
        sendDingMessage(MarkdownMessage("Launcher：$title", "# $title\n${text}$name", arrayList))
    }

    fun sendText(text: String, vararg ats: Linkman) {
        if (text == mLastText && System.currentTimeMillis() - mLastTime < 60 * 1000) {
            return
        }
        mLastText = text
        mLastTime = System.currentTimeMillis()
        val arrayList = ArrayList<String>()
        var name = ""
        if (ats.isNotEmpty()) {
            name = "\n"
            ats.forEach {
                arrayList.add(it.phoneNumber)
                name += ("@${it.phoneNumber} ")
            }
        }
        sendDingMessage(TextMessage(text + name, arrayList))
    }

    private fun sendDingMessage(message: DingMessage) {
        val timestamp = System.currentTimeMillis()
        val sign = try {
            sign(timestamp)
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }
        val url = "$WEBHOOK&timestamp=${timestamp}&sign=${sign}"
        // 网络请求，用同步方案
    }

    @Throws(Exception::class)
    fun sign(timestamp: Long): String {
        val stringToSign = "$timestamp\n$SECRET"
        val mac: Mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(SECRET.toByteArray(), "HmacSHA256"))
        val signData: ByteArray = mac.doFinal(stringToSign.toByteArray())
        return URLEncoder.encode(Base64.encodeToString(signData, Base64.NO_WRAP), "UTF-8")
    }
}