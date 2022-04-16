package io.keyss.library.common.ding

import android.util.Base64
import android.util.Log
import io.keyss.library.common.network.RequestUtil
import io.keyss.library.common.utils.GsonUtil
import java.net.URLEncoder
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * @author Key
 * Time: 2021/05/25 17:13
 * Description:
 */
object DingUtil {
    private lateinit var mWebhook: String
    private lateinit var mSecret: String

    private var mLastText: String = ""
    private var mLastTime: Long = 0

    /**
     * 多少时间内只允许发送一条相同的消息，默认1分钟
     */
    var allowSendTheSameTextTime = 60 * 1_000

    /**
     * md发送模版
     */
    var markdownTemplate = ""


    fun init(webhook: String, secret: String) {
        mWebhook = webhook
        mSecret = secret
    }

    /**
     * 发送模版消息
     */
    fun sendTemplateMarkdown(title: String, message: String) {
        sendMarkdown(title, applyMarkdownTemplate(message))
    }

    /**
     * 套用模版
     */
    fun applyMarkdownTemplate(message: String): String = "$markdownTemplate\n$message"

    fun sendMarkdown(title: String, text: String, vararg ats: Linkman) {
        // 同一条消息一分钟内只发一次
        if (text == mLastText && (System.currentTimeMillis() - mLastTime < allowSendTheSameTextTime)) {
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
        sendDingMessage(MarkdownMessage(title, "# $title\n${text}$name", arrayList))
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

    /**
     * 耗时，请在子现场调用
     */
    private fun sendDingMessage(message: DingMessage) {
        if (!::mWebhook.isInitialized || !::mSecret.isInitialized) {
            Log.e("DingUtil", "请先初始化Webhook和Secret后再使用")
            return
        }
        val timestamp = System.currentTimeMillis()
        val sign = try {
            sign(timestamp)
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }
        val url = "$mWebhook&timestamp=${timestamp}&sign=${sign}"

        // 网络请求，用同步方案
        GsonUtil.toJson(message).takeIf { it.isNotBlank() }?.let {
            RequestUtil.postJson(url, it)
        }
    }

    @Throws(Exception::class)
    fun sign(timestamp: Long): String {
        val stringToSign = "$timestamp\n$mSecret"
        val mac: Mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(mSecret.toByteArray(), "HmacSHA256"))
        val signData: ByteArray = mac.doFinal(stringToSign.toByteArray())
        return URLEncoder.encode(Base64.encodeToString(signData, Base64.NO_WRAP), "UTF-8")
    }
}