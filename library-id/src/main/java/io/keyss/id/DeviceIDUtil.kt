package io.keyss.id

import android.app.Application
import android.content.Context
import android.util.Log
import com.baidu.liantian.ac.LH
import java.util.*

/**
 * @author Key
 * Time: 2022/01/17 10:33
 * Description:
 */
object DeviceIDUtil {
    private const val TAG = "DeviceIDUtil"

    private var mDeviceUniqueID: String? = null

    @JvmStatic
    fun getDeviceUniqueID(context: Context?): String? {
        if (null == mDeviceUniqueID) {
            //val applicationContext = if (null == context) ApplicationUtil.getApplication() else context.applicationContext
            val applicationContext = context?.applicationContext ?: getCurrentApplicationByReflect()
            mDeviceUniqueID = try {
                LH.init(applicationContext, false)
                val deviceId = LH.getId(applicationContext, "1")
                deviceId.second.uppercase(Locale.getDefault())
            } catch (e: Exception) {
                Log.e(TAG, "Load liantian ac failed", e)
                null
            }
        }
        return mDeviceUniqueID
    }

    /**
     * 仅供该包内使用
     */
    private fun getCurrentApplicationByReflect(): Application? {
        return try {
            Class.forName("android.app.ActivityThread")
                .getMethod("currentApplication")
                .invoke(null) as? Application?
        } catch (e: java.lang.Exception) {
            Log.e(TAG, "Reflect Application failed", e)
            null
        }
    }
}