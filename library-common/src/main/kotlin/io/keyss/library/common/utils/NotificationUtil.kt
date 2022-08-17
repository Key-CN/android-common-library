package io.keyss.library.common.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat

/**
 * @author Key
 * Time: 2022/08/17 17:28
 * Description:
 */
object NotificationUtil {
    /**
     * 是否有通知权限
     */
    fun areNotificationsEnabled(context: Context): Boolean {
        val notification = NotificationManagerCompat.from(context)
        return notification.areNotificationsEnabled()
    }

    /**
     * 打开系统的通知设置界面
     */
    fun openNotificationSettingsForApp(context: Context, result: (Boolean) -> Unit) {
        // Links to this app's notification settings.
        val intent = Intent()
        //Settings.ACTION_APP_NOTIFICATION_SETTINGS
        intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
        intent.putExtra("app_package", context.packageName)
        intent.putExtra("app_uid", context.applicationInfo.uid)
        // for Android 8 and above
        intent.putExtra("android.provider.extra.APP_PACKAGE", context.packageName)
        //context.startActivity(intent)
        ActivityManager.launchForTopActivityStartActivityForResult(intent) {
            val areNotificationsEnabled = areNotificationsEnabled(context)
            Log.d("NotificationUtil", "openNotificationSettingsForApp() areNotificationsEnabled=$areNotificationsEnabled")
            result(areNotificationsEnabled)
        }
    }
}