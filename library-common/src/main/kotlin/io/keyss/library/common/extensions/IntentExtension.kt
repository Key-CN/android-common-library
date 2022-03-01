package io.keyss.library.common.extensions

import android.os.Bundle

/**
 * @author Key
 * Time: 2022/03/01 13:17
 * Description:
 */

fun Bundle.toMap(): MutableMap<String, Any?> {
    val mutableMap = mutableMapOf<String, Any?>()
    for (k in keySet()) {
        mutableMap[k] = get(k)
    }
    return mutableMap
}

fun Bundle.string(): String {
    val sb = StringBuilder()
    for (k in keySet()) {
        sb.append("\nKey = [${k}]\nValue = ${get(k)}")
    }
    return sb.toString()
}