package io.keyss.library.test

import android.Manifest
import android.hardware.Camera
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import io.keyss.library.aliyun.Log
import io.keyss.library.common.base.BaseReflectBindingActivity
import io.keyss.library.common.ding.DingUtil
import io.keyss.library.common.network.NetworkUtil
import io.keyss.library.common.utils.ActivityManager
import io.keyss.library.common.utils.ShellUtil
import io.keyss.library.test.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : BaseReflectBindingActivity<ActivityMainBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

            // tty
            sb.appendLine("tty")
            val ttyResult = ShellUtil.executeShell("ls /dev | grep tty")
            sb.appendLine(ttyResult.text)
            sb.appendLine()
            sb.appendLine()

            sb.appendLine("摄像头数量：${Camera.getNumberOfCameras()}")

            sb.appendLine()
            sb.appendLine("byApi")
            val interfaceConfigByApi = NetworkUtil.getInterfaceConfigByApi(this@MainActivity)
            sb.appendLine(interfaceConfigByApi)
            sb.appendLine("byShell")
            val interfaceConfigByShell = NetworkUtil.getInterfaceConfigByShell(this@MainActivity)
            sb.appendLine(interfaceConfigByShell)
            sb.appendLine()
            sb.appendLine("ActiveInterface")
            val activeInterfaceConfig = NetworkUtil.getActiveInterfaceConfig(this@MainActivity)
            sb.appendLine(activeInterfaceConfig)

            sb.appendLine()
            sb.appendLine("mac: [${NetworkUtil.getHardwareAddress("wlan0")}]")
            //sb.appendLine("DeviceID=${DeviceIDUtil.getDeviceUniqueID()}")
            sb.appendLine()
            sb.appendLine("网卡列表")
            Log.i("")
            sb.appendLine(NetworkUtil.getAllInterfaceList())
            mBinding.tvMainActivity.text = sb.toString()
        }
        //sendDing()
        testPermission()
    }

    private fun testPermission() {
        Log.i("testPermission")
        // 启动一个等待返回的
        ActivityManager.launchForTopActivityRequestPermission(Manifest.permission.ACTIVITY_RECOGNITION) {
            Log.i("checkAndRequestPermission() ACTIVITY_RECOGNITION=[${it}]")
            if (it) {
                Log.i("onPermissionPassed")
            } else {
                Log.i("不授予权限无法获取步数！")
            }
        }
    }

    private fun sendDing() {
        lifecycleScope.launch(Dispatchers.IO) {
            val WEBHOOK = "https://oapi.dingtalk.com/robot/send?access_token=382bd3fdadfc29aae77754bafc89a8562e8c13854de3213e383cd97aa4cef4b5"
            val SECRET = "SEC077ca37187a3bf68a0351c06b57daf2f3d00cd2cb41611ad07996463dc0724e3"
            DingUtil.init(WEBHOOK, SECRET)
            DingUtil.sendMarkdown(
                "test",
                """
                    * ${DingUtil.applyRed("applyRed~~~~~~~~~~")}
                    * ${DingUtil.applyGreen("applyGreen~~~~~~~~~~")}
                    * ${DingUtil.applyBlue("applyBlue~~~~~~~~~~")}
                    * ${DingUtil.applyYellow("applyYellow~~~~~~~~~~")}
                    * ${DingUtil.applyCyan("applyCyan~~~~~~~~~~")}
                    * ${DingUtil.applyMagenta("applyMagenta~~~~~~~~~~")}
                """.trimIndent()
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}