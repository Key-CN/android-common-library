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