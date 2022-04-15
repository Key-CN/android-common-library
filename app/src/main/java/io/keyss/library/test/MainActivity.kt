package io.keyss.library.test

import android.hardware.Camera
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import io.keyss.id.DeviceIDUtil
import io.keyss.library.aliyun.Log
import io.keyss.library.common.base.BaseReflectBindingActivity
import io.keyss.library.common.extensions.string
import io.keyss.library.common.utils.*
import io.keyss.library.test.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

class MainActivity : BaseReflectBindingActivity<ActivityMainBinding>() {
    companion object {
        private var staticTime = 0
    }

    private var memoryTime = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        memoryTime++
        staticTime++
        Log.w(
            "!!!!!!!!!!!!!!! MainActivity(${this.hashCode()}) onCreate !!!!!!!!!!!!!!!" +
                    "\nmemoryTime=$memoryTime, staticTime=$staticTime, globalTime=${App.globalTime}" +
                    "\n${intent.extras?.string()}"
        )
        //requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        super.onCreate(savedInstanceState)
        aliyunTest()
        thread {
            Log.i("子线程测试，DeviceID=${DeviceIDUtil.getDeviceUniqueID(null)}")
        }
        lifecycleScope.launchWhenStarted {
            val sb = StringBuilder()
            sb.appendLine(
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS", Locale.SIMPLIFIED_CHINESE).format(Date())
            )
            sb.appendLine()
            /*sb.appendLine(withContext(Dispatchers.IO) {
                ScreenUtil.printScreenInfo()
            })
            sb.appendLine()
            sb.appendLine(withContext(Dispatchers.IO) {
                NetworkUtil.getWifiInfo(this@MainActivity)
            })*/
            sb.appendLine("摄像头数量：${Camera.getNumberOfCameras()}")
            sb.appendLine()
            //sb.appendLine("DeviceID=${DeviceIDUtil.getDeviceUniqueID()}")
            mBinding.tvMainActivity.text = sb.toString()
        }

        //requestPermissions(arrayOf(Manifest.permission.CAMERA), 909)
        /* var beginTransaction = supportFragmentManager.beginTransaction()
         val fragment = EmptyFragment()
         beginTransaction.add(R.id.fragment_main_activity, fragment)
         beginTransaction.commitAllowingStateLoss()*/

        /*Looper.myQueue().addIdleHandler {

            false
        }*/
        /*val emptyFragment = EmptyFragment()
        lifecycleScope.launchWhenResumed {
            delay(3000)
            beginTransaction = supportFragmentManager.beginTransaction()
            beginTransaction.replace(R.id.fragment_main_activity, emptyFragment)
            beginTransaction.commitAllowingStateLoss()

            delay(2000)
            beginTransaction = supportFragmentManager.beginTransaction()
            beginTransaction.replace(R.id.fragment_main_activity, cameraFragment)
            beginTransaction.commitAllowingStateLoss()
        }*/

        val executeSuShell = ShellUtil.executeSuShell("pwd", "cd sdcard", "pwd", "ls")
        Log.i("executeSuShell=$executeSuShell")

        /*lifecycleScope.launch(Dispatchers.IO) {
            DingUtil.WEBHOOK = "https://oapi.dingtalk.com/robot/send?access_token=382bd3fdadfc29aae77754bafc89a8562e8c13854de3213e383cd97aa4cef4b5"
            DingUtil.SECRET = "SEC077ca37187a3bf68a0351c06b57daf2f3d00cd2cb41611ad07996463dc0724e3"
            DingUtil.sendText("test")
        }*/
        /*PlaySoundUtil.init(this)
        //PlaySoundUtil.defaultSpeed = 1.9f
        lifecycleScope.launch(Dispatchers.IO) {
            //test0123()
            delay(1_000)
            PlaySoundUtil.playRawSound(
                //R.raw.result_health_info_normal,
                213168123,
                isExistSameOverwrite = false,
                isCutAndTop = false,
                isCurrentReplay = false
            )
            PlaySoundUtil.playRawSound(true, 1223412341, R.raw.result_health_info_normal)
            delay(1_000)
            PlaySoundUtil.setSpeed(2f, true)
        }*/
        TTSUtil.init(this)
        lifecycleScope.launch(Dispatchers.Main) {
            delay(1000)
            TTSUtil.speak("测温失败，请重新开始！")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.w("!!!!!!!!!!!!!!! MainActivity(${this.hashCode()}) onDestroy !!!!!!!!!!!!!!!\nmemoryTime=$memoryTime, staticTime=$staticTime, globalTime=${App.globalTime}")
    }

    private suspend fun test0123() {
        delay(3_000)
        ApplicationUtil.restartAppHard(this)
//        ApplicationUtil.restartAppSoft(this)
        Log.i("currentTopActivity=${ActivityUtil.getTopActivityComponentName(this)}")
    }

    private fun aliyunTest(): Unit {
        /*Log.init(
            this, "projectName", "storeName", "AndroidTest",
            {
                AliYunLogSTSBean("xxxxxxxxx", "xxxxxxxxxxxx")
            },
            true,
            logTags = mapOf(
                Pair("Version", "${BuildConfig.VERSION_NAME}(${BuildConfig.VERSION_CODE})")
            ),
            fixedDynamicContentsBlock = mapOf(
                Pair("Current", { System.currentTimeMillis().toString() })
            ),
            aliyunLogEndPoint = "cn-hangzhou.log.aliyuncs.com"
        )*/
        /*Log.v(21341234123412)
        Log.d("debug")
        Log.i("info")
        Log.w(Date())
        Log.e("error!!!!!")
        Log.e("error!!!!!!!!!!!", Exception("自定义"))*/
    }
}