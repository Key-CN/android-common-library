package io.keyss.library.test

import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.keyss.library.common.utils.ShellUtil
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
        System.err.println("androidTest start")
        // Context of the app under test.
        //val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        //Assert.assertEquals(BuildConfig.APPLICATION_ID, appContext.packageName)

        val executeSuShell = ShellUtil.executeSuShell("java --version", "javac --version")
        println("androidTest executeSuShell=$executeSuShell")

        SystemClock.sleep(3_000)
        System.err.println("androidTest end")
    }
}