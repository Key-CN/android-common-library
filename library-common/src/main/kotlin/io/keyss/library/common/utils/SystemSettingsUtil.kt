package io.keyss.library.common.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager

/**
 * @author Key
 * Time: 2022/04/06 20:41
 * Description:
 */
object SystemSettingsUtil {

    /**
     * 仅打开Wi-Fi设置
     */
    @Throws(ActivityNotFoundException::class)
    fun startWifiSettingActivity(context: Context) {
        /*val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
        intent.putExtra("extra_prefs_show_button_bar", true)
        intent.putExtra("extra_prefs_set_next_text", "完成")
        intent.putExtra("extra_prefs_set_back_text", "返回")*/
        val intent = Intent(WifiManager.ACTION_PICK_WIFI_NETWORK)
        intent.putExtra("only_access_points", true)
        intent.putExtra("extra_prefs_show_button_bar", true)
        intent.putExtra("extra_prefs_set_next_text", "完成")
        intent.putExtra("extra_prefs_set_back_text", "返回")
        intent.putExtra("wifi_enable_next_on_connect", true)
        context.startActivity(intent)
    }
}