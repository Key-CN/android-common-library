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
 * Description: 每个机器人每分钟最多发送20条消息到群里，如果超过20条，会限流10分钟。
 */
object DingUtil {
    private const val WEBHOOK_PREFIX = "https://oapi.dingtalk.com/robot/send?access_token="

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


    /**
     * @param webhook 包含accessToken
     */
    fun init(webhook: String, secret: String) {
        mWebhook = webhook
        mSecret = secret
    }

    fun initByAccessToken(accessToken: String, secret: String) {
        mWebhook = WEBHOOK_PREFIX + accessToken
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
        if (!::mWebhook.isInitialized || mWebhook.isNullOrBlank()) {
            Log.e("DingUtil", "请先初始化后再使用")
            return
        }
        val timestamp = System.currentTimeMillis()
        val sign = try {
            sign(timestamp)
        } catch (e: Exception) {
            Log.e("DingUtil", "签名出错", e)
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
        if (!::mSecret.isInitialized || mSecret.isNullOrBlank()) {
            throw Exception("Secret未初始化")
        }
        val stringToSign = "$timestamp\n$mSecret"
        val mac: Mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(mSecret.toByteArray(), "HmacSHA256"))
        val signData: ByteArray = mac.doFinal(stringToSign.toByteArray())
        return URLEncoder.encode(Base64.encodeToString(signData, Base64.NO_WRAP), "UTF-8")
    }

    /**
     * @param color # 开头的RGB16进制
     */
    fun applyColor(text: String, color: String): String {
        return "<font color=\"${color}\">${text}</font>"
    }

    fun applyRed(text: String): String {
        return applyColor(text, "#F0524F")
    }

    fun applyGreen(text: String): String {
        return applyColor(text, "#5C962C")
    }

    fun applyBlue(text: String): String {
        return applyColor(text, "#3993D4")
    }

    fun applyYellow(text: String): String {
        return applyColor(text, "#A68A0D")
    }

    fun applyCyan(text: String): String {
        return applyColor(text, "#00A3A3")
    }

    fun applyMagenta(text: String): String {
        return applyColor(text, "#A771BF")
    }
}