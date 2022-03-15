package io.keyss.library.common.utils

import android.app.Activity
import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
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
}