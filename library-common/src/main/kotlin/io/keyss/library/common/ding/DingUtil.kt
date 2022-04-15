package io.keyss.library.common.ding

import android.util.Base64
import io.keyss.library.common.utils.GsonUtil
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URLEncoder
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * @author Key
 * Time: 2021/05/25 17:13
 * Description:
 */
object DingUtil {
    private val okHttpClient by lazy {
        OkHttpClient()
    }

    var WEBHOOK = ""
    var SECRET = ""

    /**
     * 多少时间内只允许发送一条相同的消息，默认1分钟
     */
    var SEND_THE_SAME_TEXT_TIME = 60 * 1_000
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
        if (text == mLastText && (System.currentTimeMillis() - mLastTime < SEND_THE_SAME_TEXT_TIME)) {
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
        val timestamp = System.currentTimeMillis()
        val sign = try {
            sign(timestamp)
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }
        val url = "$WEBHOOK&timestamp=${timestamp}&sign=${sign}"

        // 网络请求，用同步方案
        val toJson = GsonUtil.toJson(message)
        val request = Request
            .Builder()
            .url(url)
            .post(toJson.toRequestBody("application/json".toMediaType()))
            .build()
        //println("sendDingMessage: requestBody=${request.body}")
        try {
            val response = okHttpClient.newCall(request).execute()
            println("sendDingMessage: code=${response.code}, body=${response.body?.string()}")
        } catch (e: Exception) {
            e.printStackTrace()
        }
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