package io.keyss.library.common.utils

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

object GsonUtil {
    private val GSON = createGson(true)

    private val GSON_NO_NULLS = createGson(false)

    private val GSON_EXCLUDE = GsonBuilder().excludeFieldsWithoutExposeAnnotation().serializeNulls().create()

    private val GSON_EXCLUDE_NO_NULLS = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()

    fun getGson(includeNulls: Boolean = false) = if (includeNulls) GSON else GSON_NO_NULLS

    private fun createGson(serializeNulls: Boolean): Gson {
        val builder = GsonBuilder()
        if (serializeNulls) builder.serializeNulls()
        return builder.create()
    }

    /**
     * 我要默认不输出null字段
     */
    fun toJson(any: Any?, includeNulls: Boolean = false): String {
        return if (includeNulls) GSON.toJson(any) else GSON_NO_NULLS.toJson(any)
    }

    fun toJsonExcludeFields(any: Any?, includeNulls: Boolean = false): String {
        return if (includeNulls) GSON_EXCLUDE.toJson(any) else GSON_EXCLUDE_NO_NULLS.toJson(any)
    }

    fun <T> fromJson(json: String?, clazz: Class<T>): T? {
        return if (json.isNullOrBlank()) {
            null
        } else {
            try {
                GSON.fromJson(json, clazz)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    fun <T> fromJson(json: String?, type: Type?): T? {
        return if (json.isNullOrBlank()) {
            null
        } else {
            try {
                GSON.fromJson<T>(json, type)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * 错误示范，类型转出来是错的
     */
    @Deprecated("Use fromJson(json, type) instead.", ReplaceWith("fromJson(json, type)"), level = DeprecationLevel.ERROR)
    fun <T> fromJson(json: String?): T? {
        return if (json.isNullOrBlank()) {
            null
        } else {
            try {
                GSON.fromJson<T>(json, object : TypeToken<T>() {}.type)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * 保修需要的字段来输入json字符串
     * @param requiredFields 需要输出的字段
     */
    fun keepRequiredFieldsToJson(obj: Any, vararg requiredFields: String): String {
        return if (requiredFields.isEmpty()) {
            toJson(obj)
        } else {
            GsonBuilder()
                .addSerializationExclusionStrategy(object : ExclusionStrategy {
                    override fun shouldSkipClass(clazz: Class<*>?): Boolean {
                        return false
                    }

                    override fun shouldSkipField(f: FieldAttributes?): Boolean {
                        return !requiredFields.contains(f?.name)
                    }
                })
                .create()
                .toJson(obj)
        }
    }
}