package io.keyss.library.test

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        val exec = Runtime.getRuntime().exec("ifconfig")
        exec.inputStream.bufferedReader().use {
            var i = 1
            it.forEachLine { lineStr ->
                println("Line $i: $lineStr")
                i++
            }
        }
    }
}