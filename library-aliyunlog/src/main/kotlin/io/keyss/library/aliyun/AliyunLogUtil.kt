package io.keyss.library.aliyun

import android.content.Context
import android.os.SystemClock
import android.util.Log
import com.aliyun.sls.android.producer.LogProducerClient
import com.aliyun.sls.android.producer.LogProducerConfig
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

    private const val ALIYUN_LOG_HZ_END_POINT: String = "http://cn-hangzhou.sls.aliyuncs.com"
    private var mTopic = "AliyunLogUtil"
    private val mFormatter: DateFormat = SimpleDateFormat("MM-dd HH:mm:ss:SSS", Locale.SIMPLIFIED_CHINESE)
    private lateinit var mConfig: LogProducerConfig
    private lateinit var mClient: LogProducerClient
    private lateinit var mUpdateSTSBlock: () -> AliYunLogSTSBean?
    private lateinit var mContentBlock: (com.aliyun.sls.android.producer.Log) -> Unit
    private val logList = LinkedList<com.aliyun.sls.android.producer.Log>()


    /** 是否立即发送 */
    private var isSendNow = 0

    /** 上传日志的等级 */
    var uploadLevel = INFO

    /** 输入到控制台日志的等级 */
    var printLevel = VERBOSE

    /** 线上环境，需要排除问题时，可以给予特定的方式打开 */
    var isPrintLogcat = true

    /**
     * 初始化，必要的设置放进入参
     */
    fun init(
        context: Context,
        projectName: String,
        logStoreName: String,
        topic: String,
        configBlock: (config: LogProducerConfig) -> Unit,
        contentBlock: (com.aliyun.sls.android.producer.Log) -> Unit,
        updateSTSBlock: () -> AliYunLogSTSBean?,
        isPrintLogcat: Boolean = true,
        uploadLevel: Int = INFO,
        printLevel: Int = VERBOSE,
        aliyunLogEndPoint: String = ALIYUN_LOG_HZ_END_POINT,
    ) {
        mTopic = topic
        AliyunLogUtil.uploadLevel = uploadLevel
        AliyunLogUtil.printLevel = printLevel
        AliyunLogUtil.isPrintLogcat = isPrintLogcat
        mConfig = LogProducerConfig(
            context,
            aliyunLogEndPoint,
            projectName,
            logStoreName,
            "",
            "",
        )
        // 指定sts token 创建config，过期之前调用resetSecurityToken重置token
        // LogProducerConfig config = new LogProducerConfig(endpoint, project, logstore, accesskeyid, accesskeysecret, securityToken);
        // 设置主题
        mConfig.setTopic(mTopic)
        // 设置tag信息，此tag会附加在每条日志上，默认只有一条 __client_ip__:36.27.84.11

        // 每个缓存的日志包的大小上限，取值为1~5242880，单位为字节。默认为1024 * 1024
        mConfig.setPacketLogBytes(1024 * 1024)
        // 每个缓存的日志包中包含日志数量的最大值，取值为1~4096，默认为1024
        mConfig.setPacketLogCount(1024)
        // 被缓存日志的发送超时时间，如果缓存超时，则会被立即发送，单位为毫秒，默认为3000
        mConfig.setPacketTimeout(3000)
        // 单个Producer Client实例可以使用的内存的上限，超出缓存时add_log接口会立即返回失败
        // 默认为64 * 1024 * 1024
        mConfig.setMaxBufferLimit(128 * 1024 * 1024)
        // 发送线程数，默认为1
        mConfig.setSendThreadCount(5)
        // 1 开启断点续传功能， 0 关闭
        // 每次发送前会把日志保存到本地的binlog文件，只有发送成功才会删除，保证日志上传At Least Once
        mConfig.setPersistent(1)
        // 持久化的文件名，需要保证文件所在的文件夹已创建。配置多个客户端时，不应设置相同文件
        mConfig.setPersistentFilePath(File(context.filesDir, "aliyunlog.dat").absolutePath)
        // 是否每次AddLog强制刷新，高可靠性场景建议打开
        mConfig.setPersistentForceFlush(1)
        // 持久化文件滚动个数，建议设置成10。
        mConfig.setPersistentMaxFileCount(10)
        // 每个持久化文件的大小，建议设置成1-10M
        mConfig.setPersistentMaxFileSize(1024 * 1024)
        // 本地最多缓存的日志数，不建议超过1M，通常设置为65536即可
        mConfig.setPersistentMaxLogCount(65536)
        // 自定义设置放最后，可以覆盖默认设置
        configBlock(mConfig)
        mContentBlock = contentBlock
        mUpdateSTSBlock = updateSTSBlock
        updateToken()
    }

    private fun createClient() {
        // 回调函数不填，默认无回调
        // 未找到老版本的鉴权过期（Unauthorized）错误码，所以暂时无法做成被动式
        mClient = LogProducerClient(mConfig) /*{ resultCode, reqId, errorMessage, logBytes, compressedBytes -> // 回调
            // resultCode       返回结果代码
            // reqId            请求id
            // errorMessage     错误信息，没有为null
            // logBytes         日志大小
            // compressedBytes  压缩后日志大小
            if (isDebug) {
                Log.w(
                    mTopic,
                    String.format(
                        "Log回调 reqId=%s, resultCode=%d, resultCodeString=%s, errorMessage=%s, logBytes=%d, compressedBytes=%d",
                        reqId,
                        resultCode,
                        LogProducerResult.fromInt(resultCode),
                        errorMessage,
                        logBytes,
                        compressedBytes,
                    )
                )
            }
            // {"errorCode":"Unauthorized","errorMessage":"The security token you provided has expired"}
            // LOG_PRODUCER_SEND_UNAUTHORIZED
            if (LogProducerResult.fromInt(resultCode) == LogProducerResult.LOG_PRODUCER_SEND_UNAUTHORIZED) {
                updateToken()
            }
        }*/
        i("日志系统初始化成功")
    }

    private fun updateToken() {
        thread {
            continuousUpdateToken()
        }
    }

    private fun continuousUpdateToken() {
        val sts = try {
            mUpdateSTSBlock()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
        if (sts == null) {
            isSendNow = 0
        } else {
            //i("sts获取成功，开始更新token")
            mConfig.resetSecurityToken(sts.accessKeyId, sts.accessKeySecret, sts.securityToken)
            // 鉴权成功则改为立刻发送
            isSendNow = 1
            if (!AliyunLogUtil::mClient.isInitialized) {
                createClient()
            }
            sendCacheLog()
        }
        nextUpdateToken(sts != null)
    }

    private fun nextUpdateToken(isCurrentSuccessful: Boolean): Unit {
        thread {
            // 有效期半小时，失败的话就1分钟后重试
            val nextTime = 60L * 1_000 * (if (isCurrentSuccessful) 25 else 1)
            printLogcat(Log.VERBOSE, "下一次更新aliyun log sts token的时间将在${nextTime}毫秒之后")
            SystemClock.sleep(nextTime)
            continuousUpdateToken()
        }
    }

    fun v(log: String, deeper: Int = 0) {
        printAndUploadLog(Log.VERBOSE, log, deeper)
    }

    fun d(log: String, deeper: Int = 0) {
        printAndUploadLog(Log.DEBUG, log, deeper)
    }

    fun i(log: String, deeper: Int = 0) {
        printAndUploadLog(Log.INFO, log, deeper)
    }

    fun w(log: String, tr: Throwable? = null, deeper: Int = 0) {
        printAndUploadLog(Log.WARN, log, deeper, tr)
    }

    fun e(log: String, tr: Throwable? = null, deeper: Int = 0) {
        printAndUploadLog(Log.ERROR, log, deeper, tr)
    }

    /** 只是打印，不上传 */
    fun print(log: String, priority: Int = DEBUG, tr: Throwable? = null) {
        printLogcat(priority, formatLogMessage(log, 0, tr))
    }

    private fun printAndUploadLog(priority: Int, log: String, deeper: Int, tr: Throwable? = null) {
        val logString = formatLogMessage(log, deeper, tr)
        // 输出
        if (isPrintLogcat && priority >= printLevel) {
            printLogcat(priority, logString)
        }
        // 上传
        if (priority >= uploadLevel) {
            pushLog(if (priority >= WARN) logString else log, getLevelString(priority))
        }
    }

    /** 美化log */
    private fun formatLogMessage(log: String, deeper: Int, tr: Throwable?): String {
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

    private fun getLogString(msg: String, deeper: Int): String {
        val thread = Thread.currentThread()
        // 要看在第几层调用, 有默认值的default算一层，lamda表达式无法准确找到位置
        // 类似于这种，栈中路径不明确，com.ishow.good_course_teacher.ui.home.login.LoginFragment$enterInit$1$invokeSuspend$$inlined$observe$1.onChanged(LiveData.kt:52)
        val stackTraceElement: StackTraceElement = thread.stackTrace[7 + deeper]
        return "| Thread: ${thread.name} | Method: ${stackTraceElement.methodName}(${stackTraceElement.fileName}:${stackTraceElement.lineNumber}) |\n$msg"
    }

    /** 生成日志并自动选择发送方式 */
    private fun pushLog(msg: String, level: String) {
        val aliyunLog = com.aliyun.sls.android.producer.Log()
        aliyunLog.putContent("Message", msg)
        aliyunLog.putContent("CreateTime", mFormatter.format(Date()))
        aliyunLog.putContent("Level", level)
        if (AliyunLogUtil::mContentBlock.isInitialized) {
            mContentBlock(aliyunLog)
        }
        if (AliyunLogUtil::mClient.isInitialized) {
            sendLog(aliyunLog)
        } else {
            cacheLog(aliyunLog)
        }
    }

    /** 上传缓存 */
    private fun sendCacheLog() {
        var cacheLog: com.aliyun.sls.android.producer.Log?
        while (popCacheLog().also { cacheLog = it } != null) {
            sendLog(cacheLog!!)
        }
    }

    /** 取出一条缓存 */
    private fun popCacheLog(): com.aliyun.sls.android.producer.Log? {
        return logList.poll()
    }

    /** 缓存日志 */
    private fun cacheLog(aliyunLog: com.aliyun.sls.android.producer.Log) {
        // 后面看吧，如果重复率太高可以去一下重
        //aliyunLog.content["Message"]
        logList.add(aliyunLog)
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