package io.keyss.library.common.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat

/**
 * @author Key
 * Time: 2022/03/07 14:14
 * Description:
 */
object ActivityUtil {

    /**
     * @return 当前系统顶层的Activity
     */
    fun getTopActivityComponentName(context: Context): ComponentName? {
        return ContextCompat.getSystemService(context, ActivityManager::class.java)?.let {
            it.getRunningTasks(1)?.get(0)?.topActivity
        }
    }

    fun getTopActivityClassName(context: Context): String? {
        return ContextCompat.getSystemService(context, ActivityManager::class.java)?.let {
            it.getRunningTasks(1)?.get(0)?.topActivity?.className
        }
    }

    /**
     * 对比的并不是实例，如果不是Single类型，则多开的Activity也可以相等
     */
    fun Activity.isCurrentTop(): Boolean {
        return componentName.equals(getTopActivityComponentName(this))
    }

    /**
     * todo 有需要再加吧，应区分为本应用内还是全部应用的Activity判断
     */
    /*fun <T : Activity> isCurrentTop(context: Context, c: Class<T>): Boolean {
        return ComponentName(context, c) == getTopActivityComponentName(context)
    }*/

    /**
     * 通过包名启动应用
     * 如果需要启动的应用在任务栈中，则直接启动的这个应用的任务栈的顶端 activity
     * 否则启动 MainActivity
     */
    fun launchApp1(context: Context, packageName: String): Boolean {
        var mainActivity: String? = null
        val packageManager = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        intent.flags = Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or Intent.FLAG_ACTIVITY_NEW_TASK
        @SuppressLint("WrongConstant", "QueryPermissionsNeeded")
        val activities = packageManager.queryIntentActivities(intent, PackageManager.GET_ACTIVITIES)
        for (info in activities) {
            if (info.activityInfo.packageName.equals(packageName)) {
                mainActivity = info.activityInfo.name
                break
            }
        }
        if (mainActivity.isNullOrEmpty()) {
            return false
        }
        intent.component = ComponentName(packageName, mainActivity)
        Log.d("ActivityUtil", "launchApp1 启动了: ${intent.component}")
        context.startActivity(intent)
        return true
    }

    /**
     * @return 只能说明存在该包名，走到了startActivity，并不能一定确认启动的成功与否
     */
    fun launchApp2(context: Context, packageName: String): Boolean {
        try {
            //通过包名启动
            val packageManager = context.packageManager
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            if (null != intent) {
                context.startActivity(intent)
                Log.d("ActivityUtil", "launchApp2 启动了: ${intent.component}")
                return true
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return false
    }
}