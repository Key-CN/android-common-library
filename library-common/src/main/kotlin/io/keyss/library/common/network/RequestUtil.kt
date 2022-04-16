package io.keyss.library.common.network

import android.util.Log
import java.net.HttpURLConnection
import java.net.URL

/**
 * @author Key
 * @date 2022/4/16 11:53 下午
 * @Description 一些简单请求的使用，省的为了一两个请求去依赖okhttp
 */
object RequestUtil {
    /**
     * 只管发，不管返回
     * @param timeout null表示无限，永不超时
     */
    @JvmStatic
    fun postJson(url: String?, body: String?, timeout: Int? = 30_000) {
        if (url.isNullOrBlank()) {
            return
        }
        Log.i("RequestUtil", "简单POST请求URL：${url}, body=${body}")
        // 因为网络请求是耗时操作，所以需要另外开启一个线程来执行该任务。
        var httpURLConnection: HttpURLConnection? = null
        try {
            httpURLConnection = URL(url).openConnection() as HttpURLConnection
            httpURLConnection.requestMethod = "POST"
            timeout?.let {
                // 默认值是0无限，A timeout of zero is interpreted as an infinite timeout.
                httpURLConnection.connectTimeout = it
                httpURLConnection.readTimeout = it
            }
            httpURLConnection.useCaches = false
            httpURLConnection.setRequestProperty("Content-Type", "application/json; charset=utf-8")

            // 设置输入
            httpURLConnection.doInput = true
            // 设置输出
            httpURLConnection.doOutput = true


            // 写body
            if (!body.isNullOrBlank()) {
                httpURLConnection.outputStream.bufferedWriter().use {
                    it.write(body)
                    it.flush()
                }
            }

            httpURLConnection.errorStream

            val sb = StringBuilder("code=${httpURLConnection.responseCode}, message=${httpURLConnection.responseMessage}")

            // 读返回
            sb.appendLine()
            if (httpURLConnection.responseCode in 200..299) {
                httpURLConnection.inputStream.bufferedReader().use {
                    // 响应的数据
                    sb.append("body=${it.readText()}")
                }
            } else {
                httpURLConnection.errorStream.bufferedReader().use {
                    // 响应的数据
                    sb.append("error=${it.readText()}")
                }
            }
            Log.i("RequestUtil", "简单POST请求返回：${sb}")
        } catch (e: Exception) {
            Log.w("RequestUtil", "简单POST请求失败", e)
        } finally {
            httpURLConnection?.disconnect()
        }
    }
}