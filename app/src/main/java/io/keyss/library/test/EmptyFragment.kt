package io.keyss.library.test

import androidx.lifecycle.LifecycleOwner
import io.keyss.library.aliyun.Log
import io.keyss.library.common.base.BaseReflectBindingFragment
import io.keyss.library.common.broadcast.UsbDevicesReceiver
import io.keyss.library.test.databinding.FragmentEmptyBinding

/**
 * @author Key
 * Time: 2022/02/21 15:36
 * Description:
 */
class EmptyFragment : BaseReflectBindingFragment<FragmentEmptyBinding>() {
    override fun initOnceViewOnBindingOnCreateView() {

    }

    override fun initViewEveryTimeOnViewCreated() {
        Log.i("this=[${this.hashCode()}], context=[${context.hashCode()}], life=[${viewLifecycleOwner.hashCode()}], as=[${(this as LifecycleOwner).hashCode()}]")
        //UsbDevicesReceiver.registerReceiverByLifecycle(this)
    }
}