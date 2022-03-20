package io.keyss.library.aliyun

import android.content.Context
import android.os.SystemClock
import android.util.Log
import com.aliyun.sls.android.producer.LogProducerClient
import com.aliyun.sls.android.producer.LogProducerConfig
import com.aliyun.sls.android.producer.LogProducerResult
import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

object AliyunLogUtil {
    const val VERBOSE = Log.VERBOSE
    const val DEBUG = Log.DEBUG
    const val INFO = Log.INFO
    const val WARN = Log.WARN
    const val ERROR = Log.ERROR

    /**
     * 杭州节点，公网入口
     * 文档里给的只有域名没有带https://，没有忽略安全的app会直接报错
     * "https://cn-hangzhou.log.aliyuncs.com"
     * */
    private const val ALIYUN_LOG_HZ_END_POINT: String = "cn-hangzhou.log.aliyuncs.com"

    private var mTopic = "AliyunLogUtil"
    private val mFormatter: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS", Locale.SIMPLIFIED_CHINESE)
    private val mUTCFormatter: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.SIMPLIFIED_CHINESE).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    private lateinit var mConfig: LogProducerConfig
    private lateinit var mClient: LogProducerClient
    private lateinit var mUpdateSTSBlock: () -> AliYunLogSTSBean?
    private var mFixedDynamicContents: Map<String, () -> String?>? = null
    private var mConfigBlock: ((config: LogProducerConfig) -> Unit)? = null
    private val mLogCacheList = LinkedList<com.aliyun.sls.android.producer.Log>()

    // 一些记录型属于，用于分析BUG
    private var resetSecurityTokenTimes = 0
    private var callApiSuccessTimes = 0
    private var callApiFailuresTimes = 0

    /**
     * 仅本地，不上传
     */
    private var isLocal = true

    /**
     * 本地密钥
     */
    private var isLocalSts = false

    /** 是否立即发送，1立即发，0稍后在发，同时也代表token有效 */
    private var isSendNow = 0

    @Volatile
    private var isTokenValid = false

    @Volatile
    private var isStsUpdating = false

    /**
     * 发送失败的缓存条数，防止死循环
     */
    private var mCacheSize = 256

    /**
     * 调用行函数所在深度，自行测试后设定
     */
    var defaultDepth = 7

    /** 上传日志的等级 */
    var uploadLevel = INFO

    /** 输入到控制台日志的等级 */
    var printLevel = VERBOSE

    /** 线上环境，需要排除问题时，可以给予特定的方式打开 */
    var isPrintLogcat = true

    /**
     * 初始化，必要的设置放进入参
     * @param updateSTSBlock 在子线程内执行，接口不需要加suspend了
     * @param isLocalSts 仅建议用于测试，放到线上不安全
     * @param logTags 在起初配置的，固定值，无法动态更新
     * @param fixedDynamicContentsBlock 固定却又动态的内容
     */
    fun init(
        context: Context,
        projectName: String,
        logStoreName: String,
        topic: String,
        updateSTSBlock: () -> AliYunLogSTSBean?,
        isLocalSts: Boolean,
        isPrintLogcat: Boolean = true,
        uploadLevel: Int = INFO,
        printLevel: Int = VERBOSE,
        aliyunLogEndPoint: String = ALIYUN_LOG_HZ_END_POINT,
        logTags: Map<String, String>? = null,
        fixedDynamicContentsBlock: Map<String, () -> String?>? = null,
    ) {
        // 只要初始化了，即时在线服务
        isLocal = false
        this.mTopic = topic
        this.uploadLevel = uploadLevel
        this.printLevel = printLevel
        this.isPrintLogcat = isPrintLogcat
        this.mUpdateSTSBlock = updateSTSBlock
        this.isLocalSts = isLocalSts
        if (!fixedDynamicContentsBlock.isNullOrEmpty()) {
            this.mFixedDynamicContents = fixedDynamicContentsBlock
        }
        if (!logTags.isNullOrEmpty()) {
            this.mConfigBlock = {
                logTags.forEach { entry ->
                    it.addTag(entry.key, entry.value)
                }
            }
        }
        this.mConfig = createConfig(context, aliyunLogEndPoint, projectName, logStoreName)
        createClient(mConfig)
        updateToken()
    }

    private fun createConfig(
        context: Context,
        aliyunLogEndPoint: String,
        projectName: String,
        logStoreName: String
    ): LogProducerConfig {
        var accessKeyID: String? = null
        var accessKeySecret: String? = null
        if (isLocalSts) {
            isSendNow = 1
            isTokenValid = true
            mUpdateSTSBlock()?.let {
                accessKeyID = it.accessKeyId
                accessKeySecret = it.accessKeySecret
            }
        }

        print(
            "阿里云日志 createConfig: projectName = [${projectName}], logStoreName = [${logStoreName}], context = [${context}], aliyunLogEndPoint = [${aliyunLogEndPoint}], ",
            WARN
        )

        val config = LogProducerConfig(
            context,
            aliyunLogEndPoint,
            projectName,
            logStoreName,
            accessKeyID,
            accessKeySecret,
        )
        // 指定sts token 创建config，过期之前调用resetSecurityToken重置token
        // LogProducerConfig config = new LogProducerConfig(endpoint, project, logstore, accesskeyid, accesskeysecret, securityToken);
        // 设置主题
        config.setTopic(mTopic)
        // 设置tag信息，此tag会附加在每条日志上，默认只有一条 __client_ip__:36.27.84.11
        //mConfig.addTag()
        // 每个缓存的日志包的大小上限，取值为1~5242880，单位为字节。默认为1024 * 1024
        //mConfig.setPacketLogBytes(1024 * 1024)
        // 每个缓存的日志包中包含日志数量的最大值，取值为1~4096，默认为1024
        //mConfig.setPacketLogCount(1024)
        // 被缓存日志的发送超时时间，如果缓存超时，则会被立即发送，单位为毫秒，默认为3000
        //mConfig.setPacketTimeout(3000)
        // 单个Producer Client实例可以使用的内存的上限，超出缓存时add_log接口会立即返回失败
        // 默认为64 * 1024 * 1024
        //mConfig.setMaxBufferLimit(64 * 1024 * 1024)
        // 发送线程数，默认为1
        //mConfig.setSendThreadCount(3)
        // 1 开启断点续传功能， 0 关闭
        // 每次发送前会把日志保存到本地的binlog文件，只有发送成功才会删除，保证日志上传At Least Once
        config.setPersistent(1)
        // 持久化的文件名，需要保证文件所在的文件夹已创建。配置多个客户端时，不应设置相同文件
        config.setPersistentFilePath(File(context.filesDir, "aliyunlog.dat").absolutePath)
        // 是否每次AddLog强制刷新，高可靠性场景建议打开 1开0关
        config.setPersistentForceFlush(1)
        // 持久化文件滚动个数，建议设置成10。
        config.setPersistentMaxFileCount(10)
        // 每个持久化文件的大小，建议设置成1-10M
        config.setPersistentMaxFileSize(1024 * 1024)
        // 本地最多缓存的日志数，不建议超过1M，通常设置为65536即可
        config.setPersistentMaxLogCount(65536)
        // 自定义设置放最后，可以覆盖默认设置
        mConfigBlock?.invoke(config)
        return config
    }

    private fun createClient(config: LogProducerConfig) {
        // 回调函数不填，默认无回调
        // 未找到老版本的鉴权过期（Unauthorized）错误码，所以暂时无法做成被动式
        mClient = LogProducerClient(config) { resultCode, reqId, errorMessage, logBytes, compressedBytes -> // 回调
            // resultCode       返回结果代码
            // reqId            请求id
            // errorMessage     错误信息，没有为null
            // logBytes         日志大小
            // compressedBytes  压缩后日志大小
            // {"errorCode":"Unauthorized","errorMessage":"The security token you provided has expired"}
            // LOG_PRODUCER_SEND_UNAUTHORIZED
            if (LogProducerResult.fromInt(resultCode) == LogProducerResult.LOG_PRODUCER_SEND_UNAUTHORIZED) {
                print(
                    "日志发送失败：${
                        String.format(
                            "Log回调 reqId=%s, resultCode=%d, resultCodeString=%s, errorMessage=%s, logBytes=%d, compressedBytes=%d",
                            reqId,
                            resultCode,
                            LogProducerResult.fromInt(resultCode),
                            errorMessage,
                            logBytes,
                            compressedBytes,
                        )
                    }",
                    ERROR
                )
                updateToken()
            }
        }
        print("日志Client初始化成功")
    }

    private fun updateToken() {
        if (isLocalSts) {
            print("本地sts, return")
            return
        }
        if (!isStsUpdating) {
            thread {
                continuousUpdateToken()
            }
        }
    }

    private fun continuousUpdateToken() {
        if (isStsUpdating) {
            return
        }
        isStsUpdating = true
        var sts: AliYunLogSTSBean? = getAliYunLogSTSBean()
        // 重试间隔的秒
        var intervalSecondRetries: Long = 30
        while (sts == null) {
            // 获取失败的，过1分钟再试
            print("阿里云STS Token获取失败，1分钟后再试", WARN)
            SystemClock.sleep(intervalSecondRetries * 1_000)
            intervalSecondRetries += 30
            sts = getAliYunLogSTSBean()
        }
        // java.lang.OutOfMemoryError: Could not allocate JNI Env
        mConfig.resetSecurityToken(sts.accessKeyId, sts.accessKeySecret, sts.securityToken)
        resetSecurityTokenTimes++
        i("日志系统token重置成功，当前已重置${resetSecurityTokenTimes}次")
        // 鉴权成功则改为立刻发送
        isSendNow = 1
        isTokenValid = true
        if (!::mClient.isInitialized) {
            createClient(mConfig)
        }
        expirationTimer(sts)
        thread { sendCacheLog() }
        isStsUpdating = false
    }

    private fun getAliYunLogSTSBean(): AliYunLogSTSBean? {
        return try {
            val stsBean = mUpdateSTSBlock()
            callApiSuccessTimes++
            stsBean
        } catch (e: Exception) {
            print("获取AliYunLogSTS失败", ERROR, e)
            callApiFailuresTimes++
            null
        }
    }

    private fun expirationTimer(sts: AliYunLogSTSBean) {
        if (sts.expiration.isNullOrBlank()) {
            // 永久
            return
        }
        thread {
            val nextTime = try {
                // 提前5分钟，且至少大于1分钟，ParseException
                val remainingTime = mUTCFormatter.parse(sts.expiration!!)!!.time - System.currentTimeMillis() - 5 * 60 * 1_000L
                print("阿里云 sts token 剩余有效期: ${remainingTime / 1000 / 60}分钟")
                remainingTime.takeIf { it > 60L * 1_000 } ?: throw Exception("过期时间数据不正确，expiration=${sts.expiration}")
            } catch (exc: Exception) {
                print("解析过期时间异常", ERROR, exc)
                // 默认成功就给25分钟
                25 * 60 * 1_000L
            }
            print("token将在${nextTime / 1000 / 60}分钟之后过期，API调用成功${callApiSuccessTimes}次，失败${callApiFailuresTimes}次")
            SystemClock.sleep(nextTime)
            isSendNow = 0
            isTokenValid = false
        }
    }

    fun setTopic(topic: String) {
        mTopic = topic
        if (::mClient.isInitialized) {
            mConfig.setTopic(topic)
        }
    }

    fun v(log: Any?, deeper: Int = 0) {
        printAndUploadLog(Log.VERBOSE, log, deeper)
    }

    fun d(log: Any?, deeper: Int = 0) {
        printAndUploadLog(Log.DEBUG, log, deeper)
    }

    fun i(log: Any?, deeper: Int = 0) {
        printAndUploadLog(Log.INFO, log, deeper)
    }

    fun w(log: Any?, tr: Throwable? = null, deeper: Int = -1) {
        printAndUploadLog(Log.WARN, log, deeper, tr)
    }

    fun e(log: Any?, tr: Throwable? = null, deeper: Int = -1) {
        printAndUploadLog(Log.ERROR, log, deeper, tr)
    }

    /** 只是打印，不上传 */
    fun print(log: Any, priority: Int = DEBUG, tr: Throwable? = null) {
        printLogcat(priority, formatLogMessage(log, 0, tr))
    }

    private fun printAndUploadLog(priority: Int, log: Any?, deeper: Int, tr: Throwable? = null) {
        val logStr = log.toString()
        val logString = formatLogMessage(logStr, deeper, tr)
        // 输出
        if (isLocal || isPrintLogcat && priority >= printLevel) {
            printLogcat(priority, logString)
        }
        // 上传
        if (!isLocal && priority >= uploadLevel) {
            pushLog(if (priority >= WARN) logString else logStr, getLevelString(priority))
        }
    }

    /** 美化log */
    private fun formatLogMessage(log: Any?, deeper: Int, tr: Throwable?): String {
        val logBuilder = StringBuilder(getLogString(log, deeper))
        if (tr != null) {
            logBuilder.append('\n').append(Log.getStackTraceString(tr))
        }
        return logBuilder.toString()
    }

    /** 打印已经美化好的log */
    private fun printLogcat(priority: Int, logString: String) {
        Log.println(priority, mTopic, logString)
    }

    private fun getLogString(msg: Any?, deeper: Int): String {
        val thread = Thread.currentThread()
        // 要看在第几层调用, 有默认值的default算一层，lamda表达式无法准确找到位置
        // 类似于这种，栈中路径不明确，com.ishow.good_course_teacher.ui.home.login.LoginFragment$enterInit$1$invokeSuspend$$inlined$observe$1.onChanged(LiveData.kt:52)
        val stackTraceElement: StackTraceElement = thread.stackTrace[defaultDepth + deeper]
        return "| Thread: ${thread.name} | Method: ${stackTraceElement.methodName}(${stackTraceElement.fileName}:${stackTraceElement.lineNumber}) |\n$msg"
    }

    /** 生成日志并自动选择发送方式 */
    private fun pushLog(msg: String, level: String) {
        val aliyunLog = com.aliyun.sls.android.producer.Log()
        aliyunLog.putContent("Message", msg)
        aliyunLog.putContent("LocalTime", mFormatter.format(Date()))
        aliyunLog.putContent("Level", level)
        mFixedDynamicContents?.forEach { entry ->
            // 排除获取出来为null的值
            entry.value.invoke()?.let {
                aliyunLog.putContent(entry.key, it)
            }
        }
        if (::mClient.isInitialized && isTokenValid) {
            sendLog(aliyunLog)
        } else {
            cacheLog(aliyunLog)
            if (!isStsUpdating) {
                updateToken()
            }
        }
    }

    /** 上传缓存 */
    private fun sendCacheLog() {
        var cacheLog: com.aliyun.sls.android.producer.Log?
        while (popCacheLog().also { cacheLog = it } != null) {
            cacheLog?.let {
                sendLog(it)
            }
        }
    }

    /** 取出一条缓存 */
    private fun popCacheLog(): com.aliyun.sls.android.producer.Log? {
        return mLogCacheList.poll()
    }

    /** 缓存日志 */
    @Synchronized
    private fun cacheLog(aliyunLog: com.aliyun.sls.android.producer.Log) {
        if (mLogCacheList.size > mCacheSize) {
            // java.util.LinkedList$ListItr.next(LinkedList.java:893)
            // LinkedList 890行 throw new NoSuchElementException(); 如果没有下一个会抛出异常，而上层不处理
            // 而且存在边删边发的情况
            try {
                mLogCacheList.removeAll {
                    it.content["Level"]?.let { level ->
                        level != "WARN" && level != "ERROR"
                    } == true
                }
            } catch (e: Exception) {
                mLogCacheList.clear()
            }
            print("缓存日志已超过512条，清空已缓存的低等级日志，剩余${mLogCacheList.size}条", WARN)
            // 清除后还大，说明有问题，直接全清
            if (mLogCacheList.size >= mCacheSize) {
                print("清空已缓存的低等级日志后剩余${mLogCacheList.size}条，执行全清", WARN)
                mLogCacheList.clear()
            }
        }
        // 后面看吧，如果重复率太高可以去一下重
        //aliyunLog.content["Message"]
        mLogCacheList.add(aliyunLog)
    }

    /** 上传日志 */
    private fun sendLog(aliyunLog: com.aliyun.sls.android.producer.Log) {
        // addLog第二个参数flush，是否立即发送，1代表立即发送，不设置时默认为0
        mClient.addLog(aliyunLog, isSendNow)
    }

    private fun getLevelString(priority: Int): String {
        return when (priority) {
            VERBOSE -> "VERBOSE"
            DEBUG -> "DEBUG"
            INFO -> "INFO"
            WARN -> "WARN"
            ERROR -> "ERROR"
            else -> "UNKNOWN"
        }
    }
}