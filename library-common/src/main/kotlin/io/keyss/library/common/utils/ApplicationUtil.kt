package io.keyss.library.common.utils

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.util.Log
import androidx.core.content.ContextCompat
import kotlin.system.exitProcess


/**
 * @author Key
 * Time: 2022/03/07 14:13
 * Description:
 */
object ApplicationUtil {
    private var mApp: Application? = null

    @JvmStatic
    fun init(app: Application) {
        if (mApp != null) {
            return
        }
        mApp = app
        ActivityManager.init(app)
    }

    @JvmStatic
    fun getApplication(): Application? {
        if (null == mApp) {
            mApp = getCurrentApplicationByReflect() ?: getApplicationByReflect()
        }
        return mApp
    }

    /**
     * 重启其他app 或 自己，but Application 不会重启，且activity只有launchMode="standard"才会生效
     */
    fun restartAppSoft(context: Context, packageName: String? = null): Boolean {
        val intent = getLaunchIntent(context, packageName)
        return if (null != intent) {
            Handler(Looper.getMainLooper()).postDelayed({
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                ContextCompat.startActivity(context, intent, null)
                Log.d("ApplicationUtil", "restartApp1 重启了: ${intent.component}")
            }, 100)
            true
        } else {
            false
        }
    }

    /**
     * 安卓11测试未生效
     */
    fun restartAppHard(c: Context, packageName: String? = null): Boolean {
        val appContext = c.applicationContext
        val intent = getLaunchIntent(appContext, packageName)
        intent?.putExtra("REBOOT", true)
        val restartIntent = PendingIntent.getActivity(appContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
        val alarmManager = ContextCompat.getSystemService(appContext, AlarmManager::class.java)
        return if (null == alarmManager) {
            false
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1000, restartIntent)
            Log.d("ApplicationUtil", "硬重启已发送: time=${System.currentTimeMillis()}, pid=${Process.myPid()}, name=${intent?.component}")
            //Process.killProcess(Process.myPid())
            exitProcess(0)
            true
        }
    }

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
        ContextCompat.startActivity(context, intent, null)
        Log.d("ApplicationUtil", "launchApp1 启动了: ${intent.component}")
        return true
    }

    /**
     * @return 只能说明存在该包名，走到了startActivity，并不能一定确认启动的成功与否
     */
    fun launchApp2(context: Context, packageName: String): Boolean {
        try {
            //通过包名启动
            val intent = getLaunchIntent(context, packageName)
            if (null != intent) {
                ContextCompat.startActivity(context, intent, null)
                Log.d("ApplicationUtil", "launchApp2 启动了: ${intent.component}")
                return true
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return false
    }

    /**
     * 获取其他包或自己的启动Intent
     */
    fun getLaunchIntent(context: Context, packageName: String? = null): Intent? {
        return context.packageManager.getLaunchIntentForPackage(packageName ?: context.packageName)
    }

    fun getCurrentApplicationByReflect(): Application? {
        return try {
            val activityThreadClass = getActivityThread()
            val app = activityThreadClass.getMethod("currentApplication").invoke(null)
            app as? Application
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getApplicationByReflect(): Application? {
        return try {
            val activityThreadClass = getActivityThread()
            val thread = getCurrentActivityThread()
            val app = activityThreadClass.getMethod("getApplication").invoke(thread)
            app as? Application
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getCurrentActivityThread(): Any? {
        return getActivityThreadInActivityThreadStaticField() ?: getActivityThreadInActivityThreadStaticMethod()
    }

    private fun getActivityThreadInActivityThreadStaticMethod(): Any? {
        return try {
            val activityThreadClass = getActivityThread()
            activityThreadClass.getMethod("currentActivityThread").invoke(null)
        } catch (e: Exception) {
            null
        }
    }

    private fun getActivityThreadInActivityThreadStaticField(): Any? {
        return try {
            val activityThreadClass = getActivityThread()
            val sCurrentActivityThreadField = activityThreadClass.getDeclaredField("sCurrentActivityThread")
            sCurrentActivityThreadField.isAccessible = true
            sCurrentActivityThreadField[null]
        } catch (e: Exception) {
            null
        }
    }

    @Throws
    private fun getActivityThread() = Class.forName("android.app.ActivityThread")
}