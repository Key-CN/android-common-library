package io.keyss.library.common.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.LruCache
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.lang.ref.SoftReference

/**
 * @author Key
 * Time: 2022/02/18 15:45
 * Description:
 */
object Base64Util {
    const val BASE64_JPEG_HEAD = "data:image/jpeg;base64,"
    const val BASE64_PNG_HEAD = "data:image/png;base64,"
    const val BASE64_AAC_HEAD = "data:audio/aac;base64,"
    const val BASE64_XMPEG_HEAD = "data:audio/x-mpeg;base64,"
    const val BASE64_XWAV_HEAD = "data:audio/x-wav;base64,"


    private var mMemoryCache: LruCache<String, Bitmap>? = null
    private var cacheSize: Int = 0

    private fun base64ToBitmap(base64Data: String, isRemoveHead: Boolean = true): Bitmap? {
        val base64String = if (isRemoveHead && base64Data.contains("base64,")) {
            base64Data.split("base64,")[1]
        } else {
            base64Data
        }
        if (cacheSize == 0) {
            // 获取到可用内存的最大值，使用内存超出这个值会引起OutOfMemory异常。
            // LruCache通过构造函数传入缓存值，以KB为单位。
            val maxMemory = Runtime.getRuntime().maxMemory() / 1024
            // 使用最大可用内存值的1/8作为缓存的大小。
            cacheSize = (maxMemory / 8).toInt()
        }

        if (mMemoryCache == null) {
            mMemoryCache = object : LruCache<String, Bitmap>(cacheSize) {
                override fun sizeOf(key: String?, bitmap: Bitmap): Int {
                    // 重写此方法来衡量每张图片的大小，默认返回图片数量。
                    return bitmap.byteCount / 1024
                }
            }
        }

        var bitmap: Bitmap? = null
        var imgByte: ByteArray? = null
        var inputStream: InputStream? = null
        try {
            mMemoryCache?.get(base64String)?.let {
                bitmap = it
            }
            if (bitmap == null) {
                imgByte = Base64.decode(base64String, Base64.NO_WRAP)
                val option = BitmapFactory.Options()
                option.inSampleSize = 2
                option.inTempStorage = ByteArray(5 * 1024 * 1024)
                inputStream = ByteArrayInputStream(imgByte)
                val softReference = SoftReference(BitmapFactory.decodeStream(inputStream, null, option))
                bitmap = softReference.get()
                softReference.clear()
                mMemoryCache?.put(base64String, bitmap)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            imgByte = null
            try {
                inputStream?.close()
                System.gc()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return bitmap
    }

    fun bitmapToBase64(bitmap: Bitmap?, head: String = ""): String? {
        var result: String? = null
        if (null != bitmap) {
            var baos: ByteArrayOutputStream? = null
            try {
                baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)

                baos.flush()
                baos.close()

                val bitmapBytes = baos.toByteArray()
                result = imageBytesToBase64(bitmapBytes, head)
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                try {
                    if (baos != null) {
                        baos.flush()
                        baos.close()
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return result
    }

    fun imageBytesToBase64(bytes: ByteArray?, head: String = ""): String? {
        return bytesToBase64(bytes)?.let {
            "${head}$it"
        }
    }

    fun bytesToBase64(bytes: ByteArray?): String? = try {
        Base64.encodeToString(bytes, Base64.NO_WRAP)
    } catch (e: Exception) {
        null
    }

    fun base64ToBytes(base64: String?): ByteArray? = try {
        Base64.decode(base64, Base64.NO_WRAP)
    } catch (e: Exception) {
        null
    }
}