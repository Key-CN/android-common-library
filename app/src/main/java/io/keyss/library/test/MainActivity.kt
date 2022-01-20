package io.keyss.library.test

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import io.keyss.library.aliyun.Log
import io.keyss.library.common.base.BaseReflectBindingActivity
import io.keyss.library.common.network.NetworkUtil
import io.keyss.library.common.utils.ScreenUtil
import io.keyss.library.test.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
            sb.appendLine(withContext(Dispatchers.IO) {
                ScreenUtil.printScreenInfo()
            })
            sb.appendLine()
            sb.appendLine(withContext(Dispatchers.IO) {
                NetworkUtil.getWifiInfo(this@MainActivity)
            })

            sb.appendLine()
            mBinding.tvMainActivity.text = sb.toString()

            aliyunTest()
        }
    }

    suspend fun aliyunTest(): Unit {
        Log.defaultDepth = 8
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
        Log.v("VVVVVVVVVVVVVVVVVVVVVVV")
        Log.d("debug")
        Log.i("info")
        Log.w("warn")
        Log.e("error!!!!!")
        Log.e("error!!!!!!!!!!!", Exception("自定义"))
    }
}