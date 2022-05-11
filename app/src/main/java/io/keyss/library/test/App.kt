package io.keyss.library.test

import android.app.Activity
import android.app.Application
import android.os.Bundle
import io.keyss.library.aliyun.Log

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
        val cb = object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                Log.i("onActivityCreated: activity = [${activity}], savedInstanceState = [${savedInstanceState}]")
            }

            override fun onActivityStarted(activity: Activity) {
                Log.i("onActivityStarted: activity = [${activity}]")
            }

            override fun onActivityResumed(activity: Activity) {
                Log.i("onActivityResumed: activity = [${activity}]")
            }

            override fun onActivityPaused(activity: Activity) {
                Log.i("onActivityPaused: activity = [${activity}]")
            }

            override fun onActivityStopped(activity: Activity) {
                Log.i("onActivityStopped: activity = [${activity}]")
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
                Log.i("onActivitySaveInstanceState: activity = [${activity}], outState = [${outState}]")
            }

            override fun onActivityDestroyed(activity: Activity) {
                Log.i("onActivityDestroyed: activity = [${activity}]")
            }
        }
        //registerActivityLifecycleCallbacks(cb)
    }
}