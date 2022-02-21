package io.keyss.library.common.utils

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService


/**
 * @author Key
 * Time: 2022/01/21 15:10
 * Description:
 */
object LocationUtil {
    /*data class Location(
        */
    /**
     * 纬度
     *//*
        val latitude: Double,
        */
    /**
     * 经度
     *//*
        val longitude: Double,
    )*/


    @JvmStatic
    fun isGpsOpen(context: Context): Boolean {
        val locationManager = getSystemService(context, LocationManager::class.java)
        return locationManager?.let {
            // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
            val gps = it.isProviderEnabled(LocationManager.GPS_PROVIDER)
            // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
            val agps = it.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            println("NetworkUtil isGpsOpen gps=${gps} agps=${agps}")
            gps || agps
        } ?: kotlin.run {
            println("NetworkUtil isGpsOpen LocationManager 获取失败")
            false
        }
    }

    /**
     * 打开GPS，请在主线程执行
     * 不try，加个toast非主线程直接蹦，方便看，必须开发期解决
     */
    @JvmStatic
    fun openGps(context: Context) {
        Toast.makeText(context, "请手动打开GPS开关", Toast.LENGTH_LONG).show()
        context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
    }

    /**
     * 请自行在主库添加权限，防止依赖库不使用也权限过多
     */
    /*@SuppressLint("MissingPermission")
    fun getLatitudeAndLongitude(context: Context): Location? {
        val locationManager = getSystemService(context, LocationManager::class.java)
        val provider = LocationManager.GPS_PROVIDER // 指定LocationManager的定位方法
        println("locationManager=${locationManager}")
        val location: Location? = locationManager?.getLastKnownLocation(provider)
        println("location=${location}")
*//*        val lat = location?.latitude //获取纬度

        val lng = location?.longitude //获取经度*//*

        return location
    }*/
}