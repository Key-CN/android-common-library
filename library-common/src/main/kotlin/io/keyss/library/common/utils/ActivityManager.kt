package io.keyss.library.common.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.util.*

/**
 * @author Key
 * Time: 2022/08/02 15:50
 * Description: Leak自己管理，无视提示，一般不用，这次增加主要是Uni中获取不到Activity
 */
@SuppressLint("StaticFieldLeak")
object ActivityManager : Application.ActivityLifecycleCallbacks {
    @Volatile
    private var isRegistered = false

    private var mCurrentTopActivity: Activity? = null
    private val mActivities = Stack<Activity>()

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

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        mActivities.push(activity)
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
    }
}