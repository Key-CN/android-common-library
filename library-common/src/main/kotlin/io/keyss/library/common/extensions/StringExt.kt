package io.keyss.library.common.extensions

/**
 * @author Key
 * Time: 2022/04/26 17:18
 * Description:
 */

/**
 * 为了字符串正确显示去掉符号位，使用Ubyte转一次
 * @param separator 分隔符，默认空格
 * @param isUpper 是否大写，默认大写
 */
fun ByteArray.toHexString(separator: CharSequence = " ", isUpper: Boolean = true) = this.joinToString(separator) { it.toHexString() }.let {
    if (isUpper) {
        it.uppercase()
    } else {
        it.lowercase()
    }
}

/**
 * 方便观看的16进制
 */
fun Byte.toHexString(): String {
    return this.toUByte().toString(16).padStart(2, '0')
}

/**
 * @param separator 分隔符，默认无
 */
fun ByteArray.toAsciiString(separator: CharSequence = "") =
    this.map { it.toInt().toChar() }.joinToString(separator)

/**
 * 错误不会throw但是值会不对
 */
fun String.passeByteArray(): ByteArray {
    val bytes = ByteArray(length / 2)
    var j = 0
    for (i in bytes.indices) {
        val c0: Char = this[j++]
        val c1: Char = this[j++]
        bytes[i] = (parse(c0) shl 4 or parse(c1)).toByte()
    }
    return bytes
}

/**
 * 会throw
 */
@Throws
fun String.passeByteArrayOrThrow(): ByteArray {
    val bytes = ByteArray(length / 2)
    var i = 0
    var j = 0
    while (i < bytes.size) {
        bytes[i++] = "${this[j++]}${this[j++]}".toUByte(16).toByte()
    }
    return bytes
}

/**
 * UByteArray kotlin中还是实验性的
 */
@Throws
@ExperimentalUnsignedTypes
fun String.passeUByteArray(): UByteArray {
    val bytes = UByteArray(length / 2)
    var i = 0
    var j = 0
    while (i < bytes.size) {
        bytes[i++] = "${this[j++]}${this[j++]}".toUByte(16)
    }
    return bytes
}

private fun parse(c: Char): Int {
    if (c >= 'a') return c - 'a' + 10 and 0x0f
    return if (c >= 'A') c - 'A' + 10 and 0x0f else c - '0' and 0x0f
}