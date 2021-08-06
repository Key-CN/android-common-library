package io.keyss.library.common.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import androidx.viewbinding.ViewBinding
import io.keyss.library.common.extensions.getViewBinding

/**
 * @author Key
 * Time: 2021/03/24 16:00
 * Description:
 */
abstract class BaseReflectBindingDialogFragment<T : ViewBinding> : DialogFragment() {

    /*private val _binding: T by lazy {
        (getViewBinding(layoutInflater) as T).also {
            LogUtil.i("inflateBinding _binding")
            isBindingCreated = true
            initViewOnce()
        }
    }*/
    private lateinit var _binding: T

    var isBindingCreated = false
        private set

    protected val mBinding
        @Throws(Throwable::class)
        get() = if (isBindingCreated) {
            _binding
        } else {
            throw Throwable("执行过早，ViewBinding未初始化")
        }

    /**
     * 不管使用哪种方式切换Fragment，都只需要走一次初始化的，其他每次都需要渲染
     */
    protected abstract fun initOnceViewOnBindingOnCreateView()

    /**
     * 每次都会走生命周期
     */
    protected abstract fun initViewEveryTimeOnViewCreated()

    /*override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        LogUtil.i("BaseReflectBindingFragment，Class=${this::class.simpleName}")
        // 先完成懒加载，Binding中的状态应该是可以被保存的
        return _binding.let {
            LogUtil.i("onCreateView，Class=${this::class.simpleName}")
            if (it is ViewDataBinding) {
                it.lifecycleOwner = this
            }
            initViewEveryTime()
            it.root
        }
    }*/

    /**
     * onCreateView -> onViewCreated -> onActivityCreated -> onStart
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewEveryTimeOnViewCreated()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (!::_binding.isInitialized) {
            (getViewBinding(inflater, container, container != null) as T).let {
                if (it is ViewDataBinding) {
                    it.lifecycleOwner = this
                }
                _binding = it
            }
            isBindingCreated = true
            initOnceViewOnBindingOnCreateView()
        } else {
            (_binding.root.parent as ViewGroup?)?.run {
                removeView(_binding.root)
            }
        }
        return _binding.root
    }
}