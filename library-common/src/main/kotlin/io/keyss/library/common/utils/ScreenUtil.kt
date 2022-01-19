package io.keyss.library.common.utils

import android.content.res.Resources


/**
 * @author Key
 * Time: 2022/01/11 13:37
 * Description:
 */
object ScreenUtil {
    fun printScreenInfo(): String {
        val displayMetrics = Resources.getSystem().displayMetrics
        val pxWidth = displayMetrics.widthPixels // 屏幕宽度（像素）
        val pxHeight = displayMetrics.heightPixels // 屏幕高度（像素）

        val density = displayMetrics.density // 屏幕密度（0.75 / 1.0 / 1.5 / 2.25 等等）
        val densityDpi = displayMetrics.densityDpi // 屏幕密度DPI（每寸像素：120/160/240/320）

        val dpWidth = pxWidth / density
        val dpHeight = pxHeight / density

        val message = "屏幕信息： [ 宽度：${pxWidth}px-${dpWidth}dp, 高度：${pxHeight}px-${dpHeight}dp, 密度比：${density}, DPI：${densityDpi} ]\n$displayMetrics"
        println(message)
        return message
    }
}