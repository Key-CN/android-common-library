package io.keyss.library.common.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import java.util.*

/**
 * @author Key
 * Time: 2022/08/02 15:50
 * Description: Leak自己管理，无视提示，一般不用，这次增加主要是Uni中获取不到Activity
 * 新增新版API的当前Activity中请求权限的方法，也不算耦合吧
 */
@SuppressLint("StaticFieldLeak")
object ActivityManager : Application.ActivityLifecycleCallbacks {
    private const val TAG = "KeyActivityManager"

    @Volatile
    private var isRegistered = false

    private var mCurrentTopActivity: Activity? = null
    private val mActivities = Stack<Activity>()

    /**
     * 保存ActivityResultApi的Key
     */
    private const val KEY_ACTIVITY_RESULT_API = "activityResultApi"

    /**
     * 保存activity和Fragment的resultLauncher
     */
    private val startActivityForResultLauncherMap: MutableMap<String, XActivityResultContract<Intent, ActivityResult>> = mutableMapOf()
    private val onePermissionLauncherMap: MutableMap<String, XActivityResultContract<String, Boolean>> = mutableMapOf()
    private val multiplePermissionLauncherMap: MutableMap<String, XActivityResultContract<Array<String>, Map<String, @JvmSuppressWildcards Boolean>>> =
        mutableMapOf()

    /**
     * 栈顶Act
     */
    fun getTopActivity(): Activity? {
        return mActivities.takeUnless { it.isEmpty() }?.safePeekOrNull()
    }

    private fun Stack<Activity>.safePeekOrNull(): Activity? {
        return try {
            peek()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 顶部可视Act
     */
    fun getTopVisibleActivity(): Activity? {
        return mCurrentTopActivity
    }

    fun init(app: Application) {
        if (isRegistered) {
            return
        }
        isRegistered = true
        ApplicationUtil.init(app)
        app.registerActivityLifecycleCallbacks(this)
    }

    private fun getTopActivityKey(): String? {
        return getTopActivity()?.intent?.getStringExtra(KEY_ACTIVITY_RESULT_API)
    }

    /**
     * 获取activityResultLauncher
     */
    private fun getTopActivityStartActivityForResultLauncher(): XActivityResultContract<Intent, ActivityResult>? {
        val activityKey = getTopActivityKey()
        return if (activityKey.isNullOrBlank()) null else startActivityForResultLauncherMap[activityKey]
    }

    private fun getTopActivityOnePermissionLauncher(): XActivityResultContract<String, Boolean>? {
        val activityKey = getTopActivityKey()
        return if (activityKey.isNullOrBlank()) null else onePermissionLauncherMap[activityKey]
    }

    private fun getTopActivityMultiplePermissionLauncher(): XActivityResultContract<Array<String>, Map<String, @JvmSuppressWildcards Boolean>>? {
        val activityKey = getTopActivityKey()
        return if (activityKey.isNullOrBlank()) null else multiplePermissionLauncherMap[activityKey]
    }

    /**
     * StartActivityForResult
     */
    fun launchForTopActivityStartActivityForResult(intent: Intent, activityResultCallback: ActivityResultCallback<ActivityResult>? = null) {
        getTopActivityStartActivityForResultLauncher()?.launch(intent, activityResultCallback)
    }

    /**
     * RequestPermission
     * 优化一下，先判断后请求
     */
    fun launchForTopActivityRequestPermission(permission: String, activityResultCallback: ActivityResultCallback<Boolean>? = null) {
        when (PermissionUtil.queryPermission(permission, getTopActivity()!!)) {
            PackageManager.PERMISSION_GRANTED -> {
                activityResultCallback?.onActivityResult(true)
            }
            -2 -> {
                activityResultCallback?.onActivityResult(false)
            }
            else -> {
                /*包括 PackageManager.PERMISSION_DENIED*/
                getTopActivityOnePermissionLauncher()?.launch(permission, activityResultCallback)
            }
        }
    }

    /**
     * RequestMultiplePermissions
     */
    fun launchForTopActivityRequestMultiplePermissions(
        permissions: Array<String>,
        activityResultCallback: ActivityResultCallback<Map<String, Boolean>>? = null
    ) {
        // 太多了，算了，懒得写判断了，直接请求吧，需要判断的业务前置吧
        getTopActivityMultiplePermissionLauncher()?.launch(permissions, activityResultCallback)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        mActivities.push(activity)
        if (activity is ComponentActivity) {
            //生成一个Key
            val activityKey = activity.javaClass.simpleName + System.currentTimeMillis()
            //添加一个默认ActivityResultLauncher
            val startActivityForResultLauncher = XActivityResultContract(activity, ActivityResultContracts.StartActivityForResult())
            val requestPermissionLauncher = XActivityResultContract(activity, ActivityResultContracts.RequestPermission())
            val requestMultiplePermissionsLauncher = XActivityResultContract(activity, ActivityResultContracts.RequestMultiplePermissions())
            //把生成的Key放到intent中，作为每一个Activity的唯一标识
            activity.intent.putExtra(KEY_ACTIVITY_RESULT_API, activityKey)
            //存放到Map中
            startActivityForResultLauncherMap[activityKey] = startActivityForResultLauncher
            onePermissionLauncherMap[activityKey] = requestPermissionLauncher
            multiplePermissionLauncherMap[activityKey] = requestMultiplePermissionsLauncher

            Log.d(TAG, "activityKey=${activityKey},activity=${activity}, 各种launcher已注册")
        }
    }

    override fun onActivityStarted(activity: Activity) {
        mCurrentTopActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {

    }

    override fun onActivityPaused(activity: Activity) {

    }

    override fun onActivityStopped(activity: Activity) {
        if (mCurrentTopActivity == activity) {
            mCurrentTopActivity = null
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

    }

    override fun onActivityDestroyed(activity: Activity) {
        mActivities.remove(activity)
        if (activity is ComponentActivity) {
            val activityKey = activity.intent.getStringExtra(KEY_ACTIVITY_RESULT_API)
            if (!activityKey.isNullOrBlank()) {
                //移除activity的resultLauncher
                startActivityForResultLauncherMap[activityKey]?.unregister()
                startActivityForResultLauncherMap.remove(activityKey)
                onePermissionLauncherMap[activityKey]?.unregister()
                onePermissionLauncherMap.remove(activityKey)
                multiplePermissionLauncherMap[activityKey]?.unregister()
                multiplePermissionLauncherMap.remove(activityKey)
                Log.d(TAG, "activityKey=${activityKey},activity=${activity}, 各种launcher已移除！！")
            }
        }
    }

    /**
     *
     */
    class XActivityResultContract<I, O>(activityResultCaller: ActivityResultCaller, activityResultContract: ActivityResultContract<I, O>) {

        private var activityResultCallback: ActivityResultCallback<O>? = null

        private val launcher: ActivityResultLauncher<I> = activityResultCaller.registerForActivityResult(activityResultContract) {
            Log.d(TAG, "XActivityResultContract=${this}, activityResultCallback=$activityResultCallback, 回调参数=$it")
            activityResultCallback?.onActivityResult(it)
        }


        /**
         * 启动
         */
        fun launch(input: I, activityResultCallback: ActivityResultCallback<O>?) {
            Log.d(TAG, "launch() called with: input = $input, activityResultCallback = $activityResultCallback")
            this.activityResultCallback = activityResultCallback
            launcher.launch(input)
        }

        /**
         * 注销
         */
        fun unregister() {
            launcher.unregister()
        }

    }
}