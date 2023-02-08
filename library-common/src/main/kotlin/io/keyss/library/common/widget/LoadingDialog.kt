package io.keyss.library.common.widget

import androidx.core.view.isGone
import androidx.fragment.app.FragmentManager
import io.keyss.library.common.base.BaseReflectBindingDialogFragment
import io.keyss.library.common.databinding.CommonDialogLoadingBinding

/**
 * 一个简单的无依赖的dialog，开发期用用的
 */
class LoadingDialog : BaseReflectBindingDialogFragment<CommonDialogLoadingBinding>() {

    @Volatile
    private var isShowing = false

    private var mMessage: String = ""

    override fun initOnceViewOnBindingOnCreateView() {
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun initViewEveryTimeOnViewCreated() {
        setMessage()
    }

    private fun setMessage() {
        mBinding.commonTvMessageLoadingDialog.isGone = mMessage.isBlank()
        mBinding.commonTvMessageLoadingDialog.text = mMessage
    }

    fun setMessage(text: String): LoadingDialog {
        this.mMessage = text
        if (isAdded) {
            setMessage()
        }
        return this
    }

    override fun dismiss() {
        dismissAllowingStateLoss()
    }

    override fun dismissAllowingStateLoss() {
        isShowing = false
        super.dismissAllowingStateLoss()
    }

    /**
     * is already * crash
     */
    @Synchronized
    override fun showNow(manager: FragmentManager, tag: String?) {
        if (isAdded || isShowing) {
            return
        }
        isShowing = true
        super.showNow(manager, tag)
    }

    override fun show(manager: FragmentManager, tag: String?) {
        showNow(manager, tag)
    }

    fun showNow(manager: FragmentManager) {
        showNow(manager, null)
    }

    fun show(manager: FragmentManager) {
        showNow(manager, null)
    }
}