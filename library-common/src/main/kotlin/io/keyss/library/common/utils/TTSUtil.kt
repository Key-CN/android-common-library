package io.keyss.library.common.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.*

/**
 * @author Key
 * Time: 2022/04/12 16:18
 * Description: 给简单使用的TextToSpeech
 */
object TTSUtil : TextToSpeech.OnInitListener {
    private lateinit var tts: TextToSpeech
    private lateinit var mApplicationContext: Context
    private var isInitSuccess = false
    private var mDefaultLanguage: Locale = Locale.CHINA
    private var mDefaultSpeed = 1.3f

    /**
     * @param language 不设默认中文
     */
    fun init(context: Context, speed: Float? = null, language: Locale? = null) {
        mApplicationContext = context.applicationContext
        //初始化tts监听对象
        tts = TextToSpeech(mApplicationContext, this)
        if (language != null) {
            mDefaultLanguage = language
        }
        if (speed != null && speed > 0) {
            mDefaultSpeed = speed
        }
    }

    /**
     * 设置语音播报速度
     */
    fun setSpeechRate(speed: Float): Boolean {
        val result = tts.setSpeechRate(speed)
        return TextToSpeech.SUCCESS == result
    }

    /**
     * 中文
     */
    override fun onInit(status: Int) {
        // 判断是否转化成功
        if (status == TextToSpeech.SUCCESS) {
            setSpeechRate(mDefaultSpeed)
            //默认设定语言为中文，原生的android貌似不支持中文。
            val result = when (TextToSpeech.LANG_AVAILABLE) {
                tts.isLanguageAvailable(mDefaultLanguage) -> {
                    tts.setLanguage(mDefaultLanguage)
                }
                tts.isLanguageAvailable(Locale.CHINA) -> {
                    tts.setLanguage(Locale.CHINA)
                }
                tts.isLanguageAvailable(Locale.CHINESE) -> {
                    tts.setLanguage(Locale.CHINESE)
                }
                tts.isLanguageAvailable(Locale.US) -> {
                    tts.setLanguage(Locale.US)
                }
                else -> {
                    TextToSpeech.LANG_NOT_SUPPORTED
                }
            }
            if (result >= TextToSpeech.LANG_AVAILABLE) {
                isInitSuccess = true
            }
            Log.i("TTSUtil", "设置语言结果：${tts.voice}=${result}")
        }
    }

    //播放语音
    fun speak(text: String?): Boolean {
        if (text.isNullOrBlank() || !isInitSuccess) {
            return false
        }
        val result = tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null) == TextToSpeech.SUCCESS
        Log.i("TTSUtil", "speak结果：${result}")
        return result
    }
}