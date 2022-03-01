package io.keyss.library.common.widget

import androidx.annotation.MainThread
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent

/**
 * @author Key
 * Time: 2022/03/01 13:49
 * Description: 本该是个抽象类，单自定义View肯定是要继承某个View的，所以只能写成接口类
 * 需要哪个生命周期实现哪个
 * 接口中的成员变量是static，这样所有子类就变成一个变量了，所以只能子类再实现一次，持有自己的
 * 思路不对，先废弃吧，接口的话，成员变量不能私有，可能会造成错误的set
 * 先改成抽象类使用吧
 */
abstract class LifecycleBinding : LifecycleObserver {
    private var mLifecycleOwner: LifecycleOwner? = null

    @MainThread
    fun setLifecycleOwner(lifecycleOwner: LifecycleOwner?) {
        if (this.mLifecycleOwner === lifecycleOwner) {
            return
        }
        this.mLifecycleOwner?.lifecycle?.removeObserver(this)
        this.mLifecycleOwner = lifecycleOwner
        lifecycleOwner?.lifecycle?.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
    }
}