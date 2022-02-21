package io.keyss.library.test

import android.os.Bundle
import android.view.View
import io.keyss.library.aliyun.Log
import io.keyss.library.common.base.BaseReflectBindingFragment
import io.keyss.library.test.databinding.FragmentCameraBinding

/**
 * @author Key
 * Time: 2022/02/21 15:36
 * Description:
 */
class CameraFragment : BaseReflectBindingFragment<FragmentCameraBinding>() {
    override fun initOnceViewOnBindingOnCreateView() {
        Log.i("CameraFragment initOnceViewOnBindingOnCreateView: ")
//        mBinding.c1pvMainActivity.setLifecycleOwner(viewLifecycleOwner)
//        mBinding.c1pvSmallMainActivity.setLifecycleOwner(viewLifecycleOwner)
    }

    override fun initViewEveryTimeOnViewCreated() {
        Log.i("CameraFragment initViewEveryTimeOnViewCreated: ")
        mBinding.c1pvMainActivity.setLifecycleOwner(viewLifecycleOwner)
        mBinding.c1pvSmallMainActivity.setLifecycleOwner(viewLifecycleOwner)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i("onViewCreated: view = [${view}], savedInstanceState = [${savedInstanceState}]")
    }

    override fun onStart() {
        super.onStart()
        Log.i("CameraFragment onStart")
    }

    override fun onStop() {
        super.onStop()
        Log.i("CameraFragment onStop")
    }

    override fun onResume() {
        super.onResume()
        Log.i("CameraFragment onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.i("CameraFragment onPause")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.i("CameraFragment onDestroyView")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("CameraFragment onDestroy")
    }
}