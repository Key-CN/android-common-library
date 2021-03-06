package io.keyss.library.common.network.interceptor


import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Response
import java.io.IOException
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit

/**
 * Created by Tony Shen on 2017/7/9.
 * Key 增加打印线程，增加添加token头 // 2020/9/10
 * 好像是从1.4.2fork的
 */
class LoggingInterceptor private constructor(private val builder: Builder) : Interceptor {

    private val isDebug: Boolean
    private val charset: Charset

    init {
        this.isDebug = builder.isDebug
        this.charset = Charset.forName("UTF-8")
    }

    /**
     * 用于中途添加或修改头
     */
    fun setHeader(name: String, value: String?) {
        Logger.print(builder, "在请求中设定$name=$value")
        if (value.isNullOrBlank()) {
            builder.removeHeader(name)
        } else {
            builder.setHeader(name, value)
        }
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {

        var request = chain.request()

        if (builder.headers.size > 0) {
            val headers = request.headers
            val names = headers.names()
            val iterator = names.iterator()
            val requestBuilder = request.newBuilder()
            requestBuilder.headers(builder.headers)
            while (iterator.hasNext()) {
                val name = iterator.next()
                headers[name]?.let {
                    requestBuilder.addHeader(name, it)
                }
            }
            request = requestBuilder.build()
        }

        if (!isDebug) {
            return chain.proceed(request)
        }

        val requestBody = request.body

        var rContentType: MediaType? = null
        if (requestBody != null) {
            rContentType = requestBody.contentType()
        }

        var rSubtype: String? = null
        if (rContentType != null) {
            rSubtype = rContentType.subtype
        }

        if (builder.requestFlag) {
            if (request.method == "GET") {
                Logger.printJsonRequest(builder, request)
            } else {
                if (subtypeIsNotFile(rSubtype)) {
                    Logger.printJsonRequest(builder, request)
                } else {
                    Logger.printFileRequest(builder, request)
                }
            }
        }

        val st = System.nanoTime()
        val response = chain.proceed(request)

        if (builder.responseFlag) {
            val requestUrl = request.url
            val chainMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - st)
            val header = response.headers.toString()
            val code = response.code
            val isSuccessful = response.isSuccessful
            val responseBody = response.body
            val contentType = responseBody?.contentType()

            var subtype: String? = null

            if (contentType != null) {
                subtype = contentType.subtype
            }

            if (subtypeIsNotFile(subtype)) {
                responseBody?.let {
                    val source = it.source()
                    source.request(Long.MAX_VALUE)
                    val buffer = source.buffer
                    val bodyString = Logger.getJsonString(buffer.clone().readString(charset))
                    Logger.printJsonResponse(builder, chainMs, isSuccessful, code, header, bodyString, requestUrl)
                }
            } else {
                Logger.printFileResponse(builder, chainMs, isSuccessful, code, header, requestUrl)
            }
        }

        return response
    }

    private fun subtypeIsNotFile(subtype: String?) = subtype != null && (subtype.contains("json")
            || subtype.contains("xml")
            || subtype.contains("plain")
            || subtype.contains("html"))

    enum class LogLevel {
        ERROR,
        WARN,
        INFO,
        DEBUG,
        VERBOSE;
    }

    class Builder {
        private var TAG = "Log_Interceptor"

        var isDebug: Boolean = false
        var enableThreadName: Boolean = true
        var requestFlag: Boolean = false
        var responseFlag: Boolean = false
        var hideVerticalLineFlag: Boolean = false
        var logLevel: LogLevel = LogLevel.INFO

        private var requestTag: String? = null
        private var responseTag: String? = null
        private val headersBuilder: Headers.Builder = Headers.Builder()

        internal val headers: Headers
            get() = headersBuilder.build()

        internal fun getTag(isRequest: Boolean): String {
            return if (isRequest) {
                if (requestTag.isNullOrBlank()) TAG else requestTag!!
            } else {
                if (responseTag.isNullOrBlank()) TAG else responseTag!!
            }
        }

        internal fun getTag(): String = TAG

        /**
         * @param name  Filed
         * @param value Value
         * @return Builder
         * * Add a field with the specified value
         */
        fun setHeader(name: String, value: String): Builder {
            headersBuilder[name] = value
            return this
        }

        fun removeHeader(name: String): Builder {
            headersBuilder.removeAll(name)
            return this
        }

        fun getHeader(name: String): String? = headersBuilder[name]

        /**
         * Set request and response each log tag
         *
         * @param tag general log tag
         * @return Builder
         */
        fun tag(tag: String): Builder {
            TAG = tag
            return this
        }

        /**
         * Set request log tag
         *
         * @param tag request log tag
         * @return Builder
         */
        fun requestTag(tag: String): Builder {
            this.requestTag = tag
            return this
        }

        /**
         * Set response log tag
         *
         * @param tag response log tag
         * @return Builder
         */
        fun responseTag(tag: String): Builder {
            this.responseTag = tag
            return this
        }

        /**
         * Set request log flag
         *
         * @return Builder
         */
        fun request(): Builder {
            this.requestFlag = true
            return this
        }

        /**
         * Set response log flag
         *
         * @return Builder
         */
        fun response(): Builder {
            this.responseFlag = true
            return this
        }

        /**
         * Set hide vertical line flag
         *
         * @return Builder
         */
        fun hideVerticalLine(): Builder {
            this.hideVerticalLineFlag = true
            return this
        }

        /**
         * Set logLevel
         *
         * @return Builder
         */
        fun logLevel(logLevel: LogLevel): Builder {
            this.logLevel = logLevel
            return this
        }

        /**
         * @param isDebug set can sending log output
         *
         * @return Builder
         */
        fun loggable(isDebug: Boolean): Builder {
            this.isDebug = isDebug
            return this
        }

        /**
         * @param enable print thread name, default = true
         *
         * @return Builder
         */
        fun printThreadName(enable: Boolean): Builder {
            this.enableThreadName = enable
            return this
        }

        fun build() = LoggingInterceptor(this)
    }
}