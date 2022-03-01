package io.keyss.id

import android.content.Context
import android.os.Build
import android.util.Log
import com.baidu.liantian.ac.LH
import io.keyss.id.utils.ApplicationUtil
import java.util.*

/**
 * @author Key
 * Time: 2022/01/17 10:33
 * Description:
 */
object DeviceIDUtil {
    private var mDeviceUniqueID: String? = null

    fun getDeviceUniqueID(context: Context): String? {
        val applicationContext = context.applicationContext
        if (null == mDeviceUniqueID) {
            mDeviceUniqueID = try {
                LH.init(applicationContext, false)
                val deviceId = LH.getId(applicationContext, "1")
                deviceId.second.uppercase(Locale.getDefault())
            } catch (e: Exception) {
                Log.e("DeviceIDUtil", "Load liantian ac failed", e)
                null
            }
        }
        return mDeviceUniqueID
    }

    fun getDeviceUniqueID(): String? {
        if (null == mDeviceUniqueID) {
            mDeviceUniqueID = getDeviceUniqueID(ApplicationUtil.getApplication())
        }
        return mDeviceUniqueID
    }


    private fun getReflectSerialNumber(): String? {
        return try {
            val c = Class.forName("android.os.SystemProperties")
            val get = c.getMethod("get", String::class.java, String::class.java)
            get.invoke(c, "ro.serialno", Build.UNKNOWN) as String
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun String?.isUnknown() = this.isNullOrBlank() || Build.UNKNOWN == this
}