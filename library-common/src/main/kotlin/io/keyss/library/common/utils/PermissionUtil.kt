package io.keyss.library.common.utils

import android.Manifest
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

    @JvmStatic
    private val NotShowRequestPermission: Array<String> = arrayOf(Manifest.permission.ACTIVITY_RECOGNITION)

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

    /**
     * 查询是否拥有该权限
     */
    fun hasPermission(permission: String, context: Context): Boolean {
        val checkSelfPermission = ActivityCompat.checkSelfPermission(context, permission)
        Log.d(TAG, "hasPermission() [$permission] = $checkSelfPermission")
        return checkSelfPermission == PackageManager.PERMISSION_GRANTED
    }

    /**
     * 查询权限
     * shouldShowRequestPermissionRationale: 弹窗过的查询才会正确展示，没弹过的只会false
     * 直译为：是否应该现实请求权限的理由，所以如果没弹过，用户也没拒绝过就不需要show理由
     * 然而通过记录的方式去实现是不现实的，因为我们无法控制整个app，只通过一个方法去请求权限（除非强制规定所有开发人员的使用规范）
     * 所以，该方法暂时废弃
     * @param permissionStr android权限字符串
     * @return 0允许，-1拒绝 or 询问，-2永久拒绝
     */
    @Deprecated("该方法暂时废弃")
    fun queryPermission(permissionStr: String, activity: Activity): Int {
        var permissionInt = ActivityCompat.checkSelfPermission(activity, permissionStr)
        if (permissionInt != PackageManager.PERMISSION_GRANTED && !NotShowRequestPermission.contains(permissionStr)) {
            // 有一些权限返回是不可以弹窗的，实际是可以的，只能自己逐步记录一下了
            // android.permission.ACTIVITY_RECOGNITION
            if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, permissionStr)) {
                permissionInt = -2
            }
        }
        Log.d(TAG, "queryPermission: $permissionStr=$permissionInt")
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