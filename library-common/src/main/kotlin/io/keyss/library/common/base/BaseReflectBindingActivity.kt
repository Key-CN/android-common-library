package io.keyss.library.common.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ViewDataBinding
import androidx.viewbinding.ViewBinding
import io.keyss.library.common.extensions.getViewBinding


/**
 * @author Key
 * Time: 2020/08/10 14:23
 * Description:
 */
abstract class BaseReflectBindingActivity<T : ViewBinding> : AppCompatActivity() {
    protected val mBinding: T by lazy {
        getViewBinding(layoutInflater) as T
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        if (mBinding is ViewDataBinding) {
            (mBinding as ViewDataBinding).lifecycleOwner = this
        }
    }
}