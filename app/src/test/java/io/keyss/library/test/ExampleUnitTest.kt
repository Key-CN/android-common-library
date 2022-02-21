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

    data class P(val id: Int = 0) {

    }

    @Test
    fun dataTest() {
        val a = arrayOf(P())
        val b = a.clone()
        println("${a === b}")
        println("${a[0] === b[0]}")
        println("${a.contentEquals(b)}")
        //a.deepCopyData()
        println("isData=${a::class.isData}")
        println("isCompanion=${a::class.isCompanion}")
        println("isAbstract=${a::class.isAbstract}")
        println("isFinal=${a::class.isFinal}")
        println("isInner=${a::class.isInner}")
        println("isSealed=${a::class.isSealed}")
        println("isValue=${a::class.isValue}")
        println("isOpen=${a::class.isOpen}")
        println("isFun=${a::class.isFun}")
    }
}