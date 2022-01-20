package io.keyss.library.test

import io.keyss.library.common.utils.HashUtil
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun hashTest() {
        val digest = HashUtil.getFileHash("/Users/key/Downloads/balenaEtcher-1.5.120.dmg", HashUtil.AlgorithmType.MD5)
        println("长度=${digest.length}, digest=${digest}")
    }

    @Test
    fun aaTest() {
        val str: Any? = null
        println()
        println("start")
        println(str.toString())
        println("end")
        println()

    }
}