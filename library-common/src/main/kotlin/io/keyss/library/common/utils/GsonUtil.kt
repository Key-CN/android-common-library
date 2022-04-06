package io.keyss.library.common.utils

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

object GsonUtil {
    private var mGsonBuilder: GsonBuilder? = null

    private val GSON by lazy { createGson(true) }

    private val GSON_NO_NULLS by lazy { createGson(false) }

    private val GSON_EXCLUDE by lazy { createGson(serializeNulls = true, requireExpose = true) }

    private val GSON_EXCLUDE_NO_NULLS by lazy { createGson(serializeNulls = false, requireExpose = true) }

    fun getGson(includeNulls: Boolean = false) = if (includeNulls) GSON else GSON_NO_NULLS

    fun getGsonBuilder(): GsonBuilder {
        return mGsonBuilder ?: GsonBuilder()
    }

    /**
     * @param serializeNulls 是否序列化Null字段，一般false，节省长度
     * @param requireExpose 排除没有Expose注解的字段，也就是是否只序列化有Expose注解的字段，默认false
     */
    private fun createGson(serializeNulls: Boolean, requireExpose: Boolean = false): Gson {
        val builder = getGsonBuilder()
        if (serializeNulls) builder.serializeNulls()
        if (requireExpose) builder.excludeFieldsWithoutExposeAnnotation()
        return builder.create()
    }

    /**
     * 为了对应php后台，添加Gson解析容错，这个写的不错，https://github.com/getActivity/GsonFactory
     * implementation 'com.github.getActivity:GsonFactory:5.2'
     * 早于第一次使用即可
     */
    fun customGson(gsonBuilder: GsonBuilder) {
        mGsonBuilder = gsonBuilder
    }

    /**
     * 我要默认不输出null字段
     */
    fun toJson(any: Any?, includeNulls: Boolean = false): String {
        return getGson(includeNulls).toJson(any)
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