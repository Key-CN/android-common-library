package io.keyss.library.common.extensions

import io.keyss.library.common.utils.GsonUtil

/**
 * @author Key
 * Time: 2022/02/21 17:56
 * Description:
 */
fun <K, V> Map<K, V>.toJson(): String? {
    return try {
        GsonUtil.toJson(this)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun <T> List<T>.toJson(): String? {
    return try {
        GsonUtil.toJson(this)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}