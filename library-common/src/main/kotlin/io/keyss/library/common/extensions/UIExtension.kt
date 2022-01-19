package io.keyss.library.common.extensions

import android.util.Log
import android.view.View

/**
 * @author Key
 * Time: 2022/01/13 18:09
 * Description:
 */

/**
 * 可全局修改防抖间隔时间
 */
var antiShakeInterval = 500

/**
 * 点击事件防抖
 */
fun View.antiShakeClick(interval: Int = antiShakeInterval, block: (v: View) -> Unit): View {
    setOnClickListener {
        val lastTime = getTag(id) as? Long ?: 0
        if (System.currentTimeMillis() - lastTime > interval) {
            // 有效点击
            setTag(id, System.currentTimeMillis())
            block(it)
        } else {
            Log.d("", "[${this.javaClass.simpleName}]防抖，此次点击已过滤，此按钮防抖时间为${interval}ms")
        }
    }
    return this
}