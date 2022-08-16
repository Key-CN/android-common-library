package io.keyss.library.test

import android.app.Application
import io.keyss.library.aliyun.Log
import io.keyss.library.common.utils.ApplicationUtil

/**
 * @author Key
 * Time: 2022/03/15 10:59
 * Description:
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.defaultDepth = 8
        Log.setTopic("common-library-test-app")
        ApplicationUtil.init(this)
    }
}