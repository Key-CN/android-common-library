package io.keyss.library.common.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * @author Key
 * Time: 2022/07/18 19:11
 * Description:
 */
object PermissionUtil {
    private const val TAG = "PermissionUtil"

    /**
     * 是否忽略电池优化
     *
     * @return false 未忽略，调用关闭
     */
    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            pm.isIgnoringBatteryOptimizations(context.packageName)
        } else {
            true
        }
    }

    /**
     * 关闭电池优化
     */
    @SuppressLint("BatteryLife")
    fun closeBatteryOptimizations(context: Context) {
        //关闭电池优化
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val ignoringBatteryOptimizations = isIgnoringBatteryOptimizations(context)
            Log.d(TAG, "closeBatteryOptimizations() SDK=" + Build.VERSION.SDK_INT + ", 已忽略电池优化=" + ignoringBatteryOptimizations)
            if (!ignoringBatteryOptimizations) {
                try {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                    intent.data = Uri.parse("package:" + context.packageName)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    ContextCompat.startActivity(context, intent, null)
                } catch (e: Exception) {
                    Log.e(TAG, "关闭电池优化失败", e)
                    //Toast.makeText(ApplicationUtil.getApplication(), "无法关闭电池优化,请手动关闭！" + e, Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.i(TAG, "已关闭电池优化")
                //Toast.makeText(ApplicationUtil.getApplication(), "已关闭电池优化", Toast.LENGTH_SHORT).show();
            }
        }
    }

    fun hasPermission(permission: String, context: Context): Boolean {
        Log.d(TAG, "hasPermission() called with: permission = [$permission]")
        return ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * 查询权限
     *
     * @param permissionStr android权限字符串
     * @return 0允许，-1拒绝 or 询问，-2永久拒绝
     */
    fun queryPermission(permissionStr: String, activity: Activity): Int {
        var permissionInt = ActivityCompat.checkSelfPermission(activity, permissionStr)
        if (permissionInt != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, permissionStr)) {
                permissionInt = -2
            }
        }
        return permissionInt
    }

    /**
     * 仅在onCreate中可用，自己写的Activity时可以用
     */
    fun requestOnePermission(permission: String, activity: ComponentActivity, resultAction: ((Boolean) -> Unit)? = null) {
        if (hasPermission(permission, activity)) {
            resultAction?.invoke(true)
        } else {
            val launcher = activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                resultAction?.invoke(it)
            }
            launcher.launch(permission)
        }
    }

    /**
     * 仅在onCreate中可用，自己写的Activity时可以用
     */
    fun requestMultiplePermissions(permissions: Array<String>, activity: ComponentActivity, resultAction: ((Map<String, Boolean>) -> Unit)? = null) {
        if (permissions.all { hasPermission(it, activity) }) {
            val map = HashMap<String, Boolean>()
            for (permission in permissions) {
                map[permission] = true
            }
            resultAction?.invoke(map)
        } else {
            val launcher = activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                resultAction?.invoke(it)
            }
            launcher.launch(permissions)
        }
    }
}