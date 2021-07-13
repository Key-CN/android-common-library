package io.keyss.library.common.utils

import java.io.BufferedInputStream
import java.io.File
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.MessageDigest

/**
 * @author Key
 * Time: 2021/06/03 11:19
 * Description: 哈希计算工具
 * Android provides the following MessageDigest algorithms:
 * Algorithm  Supported API Levels
 * MD5      1+
 * SHA-1    1+
 * SHA-224  1-8,22+
 * SHA-256  1+
 * SHA-384  1+
 * SHA-512  1+
 */
object HashUtil {
    enum class AlgorithmType(val algorithmName: String) {
        MD5("MD5"),
        SHA_1("SHA-1"),
        SHA_224("SHA-224"),
        SHA_256("SHA-256"),
        SHA_384("SHA-384"),
        SHA_512("SHA-512")
    }

    @JvmStatic
    @Throws(GeneralSecurityException::class)
    fun stringToMD5(string: String): String {
        return bytesToMD5(string.encodeToByteArray())
    }

    @JvmStatic
    @Throws(GeneralSecurityException::class)
    fun bytesToMD5(bytes: ByteArray): String {
        val messageDigest = MessageDigest.getInstance(AlgorithmType.MD5.algorithmName)
        messageDigest.update(bytes)
        return digestString(messageDigest.digest())
    }

    @JvmStatic
    @Throws(Exception::class)
    fun getFileMD5(file: File): String {
        return getFileHash(file, AlgorithmType.MD5)
    }

    @JvmStatic
    @Throws(Exception::class)
    fun getFileHash(path: String, algorithm: AlgorithmType): String {
        return getFileHash(File(path), algorithm)
    }

    @JvmStatic
    @Throws(Exception::class)
    fun getFileHash(file: File, algorithm: AlgorithmType): String {
        if (!file.exists()) {
            throw IOException(file.absolutePath + "：文件不存在")
        }
        if (!file.isFile) {
            throw IOException(file.absolutePath + "：不是一个文件")
        }
        if (!file.canRead()) {
            throw IOException(file.absolutePath + "：无法读取")
        }
        val messageDigest = MessageDigest.getInstance(algorithm.algorithmName)
        return BufferedInputStream(file.inputStream()).use { bis ->
            var len: Int
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (bis.read(buffer).also { len = it } != -1) {
                messageDigest.update(buffer, 0, len)
            }
            return@use digestString(messageDigest.digest())
        }
    }

    /**
     * 摘要以16进制字符串显示
     */
    @JvmStatic
    private fun digestString(digest: ByteArray): String {
        val sb = StringBuffer()
        for (byte in digest) {
            //获取低八位有效值
            val i: Int = byte.toInt() and 0xff
            if (i < 16) {
                //如果是一位的话，补0
                sb.append("0")
            }
            //将整数转化为16进制
            sb.append(Integer.toHexString(i))
        }
        //println("ByteArray长度=${digest.size}, 16进制长度=${sb.length}")
        return sb.toString()
    }
}