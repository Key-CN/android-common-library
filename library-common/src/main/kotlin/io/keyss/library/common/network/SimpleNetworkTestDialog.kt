package io.keyss.library.common.network

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import io.keyss.library.common.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * @author Key
 * Time: 2021/07/07 17:39
 * Description:
 */
class SimpleNetworkTestDialog(specifiedDestination: String? = null) : DialogFragment() {
    lateinit var mTextView: AppCompatTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_network_test_simple, container, false)
        mTextView = view.findViewById<AppCompatTextView>(R.id.tv_simple_test_network_dialog)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launchWhenResumed {
            val result = withContext(Dispatchers.IO) {
                NetworkUtil.executeTesting()
            }
            mTextView.text = result.getStepDetail()
        }
    }
}