package io.keyss.library.common.utils

import android.app.Application

/**
 * @author Key
 * Time: 2022/03/07 14:13
 * Description:
 */
object ApplicationUtil {
    private var mApp: Application? = null

    fun getApplication(): Application? {
        if (null == mApp) {
            mApp = getCurrentApplicationByReflect() ?: getApplicationByReflect()
        }
        return mApp
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