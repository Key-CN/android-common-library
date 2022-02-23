package io.keyss.library.test

import android.Manifest
import android.content.Intent
import android.content.pm.ActivityInfo
import android.hardware.Camera
import android.os.Bundle
import android.os.Looper
import android.view.ViewTreeObserver
import androidx.lifecycle.lifecycleScope
import io.keyss.library.aliyun.Log
import io.keyss.library.common.base.BaseReflectBindingActivity
import io.keyss.library.common.utils.AverageUtil
import io.keyss.library.test.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.system.measureTimeMillis

class MainActivity : BaseReflectBindingActivity<ActivityMainBinding>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        super.onCreate(savedInstanceState)
        aliyunTest()
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
            mBinding.tvMainActivity.text = sb.toString()


        }

        requestPermissions(arrayOf(Manifest.permission.CAMERA), 909)
        var beginTransaction = supportFragmentManager.beginTransaction()
        val cameraFragment = CameraFragment()
        beginTransaction.add(R.id.fragment_main_activity, cameraFragment)
        beginTransaction.commitAllowingStateLoss()

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
    }

    private fun testTree() {
        lifecycleScope.launchWhenResumed {
            delay(1000)
            repeat(5) {
                testTreeCore(it)
                delay(100)
            }
        }
    }

    private fun testTreeCore(i: Int) {
        Log.e("testTreeCore sw- $i")
        val start = System.currentTimeMillis()
        mBinding.tvMainActivity.viewTreeObserver.addOnDrawListener(object : ViewTreeObserver.OnDrawListener {
            override fun onDraw() {
                Log.e("testTreeCore s- $i, ${System.currentTimeMillis() - start}ms")
                mBinding.tvMainActivity.viewTreeObserver.removeOnDrawListener(this)
                Log.e("testTreeCore e- $i, ${System.currentTimeMillis() - start}ms")
            }
        })
        Log.e("testTreeCore ew- $i")
    }

    private fun testAsync() {
        lifecycleScope.launch {

            val s1 = try {
                suspendCoroutine<String> {
                    a1 { s ->
                        it.resumeWithException(Exception("hahahaha"))
                    }
                }
            } catch (e: Exception) {
            }
            Log.i("1=${s1}")
            val s2 = suspendCoroutine<String> {
                a1 { s ->
                    //it.resume(s)
                }
            }

            Log.i("2=${s2}")
            throw Exception("xixixix")
        }
    }

    fun a1(l: (String) -> Unit) {
        l.invoke("test")
    }

    private fun testArrayCopy() {
        lifecycleScope.launch(Dispatchers.IO) {
            delay(2_000)
            val b = ByteArray(1280 * 720 * 12 / 8) { it.toByte() }
            val avg = AverageUtil()
            // 640*480:460800, 1280 * 720: 1382400
            Log.i("开始测试深拷贝, isData=${b::class.isData}, size=${b.size}")

            delay(500)
            //var b2 = b.clone()
            /*var b2 = ByteArray(b.size)
            System.arraycopy(b, 0, b2, 0, b.size)*/
            var b2 = b.copyOf(b.size)
            Log.i("循环外 深拷贝，是否相等=${b.hashCode() == b2.hashCode()}, 是否相等=${b === b2}, 内容是否相等=${b2.contentEquals(b)}")
            delay(500)
            repeat(1001) {
                val number = measureTimeMillis {
                    // 本次耗时=7ms, min=1.0, max=12.0, avg=1.6014127144298689, count=991
                    //b.clone()
                    //    本次耗时=1ms, min=1.0, max=14.0, avg=1.5832492431886982, count=991
                    /*b2 = ByteArray(b.size)
                    System.arraycopy(b, 0, b2, 0, b.size)*/
                    //     本次耗时=2ms, min=1.0, max=14.0, avg=1.6125126135216952, count=991
                    //b2 = b.copyOf(b.size)
                    //Log.i("深拷贝，是否相等=${b.hashCode() == deepCopy.hashCode()}, 内容是否相等=${deepCopy.contentEquals(b)}")
                }
                avg.add(number)
                if (it % 10 == 0) {
                    Log.i("本次耗时=${number}ms, ${avg}")
                }
            }
        }
    }

    private fun aliyunTest(): Unit {
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
        /*Log.v(21341234123412)
        Log.d("debug")
        Log.i("info")
        Log.w(Date())
        Log.e("error!!!!!")
        Log.e("error!!!!!!!!!!!", Exception("自定义"))*/
    }
}