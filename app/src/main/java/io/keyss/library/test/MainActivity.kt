package io.keyss.library.test

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.keyss.library.common.base.BaseReflectBindingActivity
import io.keyss.library.common.network.NetworkUtil
import io.keyss.library.common.utils.ScreenUtil
import io.keyss.library.test.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
                NetworkUtil.getArp()
            })
            mBinding.tvMainActivity.text = sb.toString()
        }
    }
}