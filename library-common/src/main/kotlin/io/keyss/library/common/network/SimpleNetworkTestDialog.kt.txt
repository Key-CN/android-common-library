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
class SimpleNetworkTestDialog(
    private val specifiedDestination: String? = null,
    private val resultBlock: ((NetworkUtil.TestResult) -> Unit)? = null
) : DialogFragment() {
    lateinit var mTvTitle: AppCompatTextView
    lateinit var mTvDetail: AppCompatTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_network_test_simple, container, false)
        mTvTitle = view.findViewById<AppCompatTextView>(R.id.tv_title_simple_test_network_dialog)
        mTvDetail = view.findViewById<AppCompatTextView>(R.id.tv_detail_simple_test_network_dialog)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launchWhenResumed {
            val result = withContext(Dispatchers.IO) {
                NetworkUtil.executeTesting(specifiedDestination)
            }
            mTvTitle.text = result.getStepDetail()
            val sb = StringBuilder()
            result.ip?.takeIf { it.isNotEmpty() }?.let {
                for (config in it) {
                    sb.append("\n活跃${config.toHumanString()}")
                }
            }
            if (!result.gateway.isNullOrBlank()) {
                sb.append("\n网关：${result.gateway}")
            }
            result.internetPing?.let {
                sb.append("\n114丢包率：${it.packetLossRate}")
            }
            result.specifiedPing?.let {
                sb.append("\n${it.destination}丢包率：${it.packetLossRate}")
            }
            mTvDetail.text = sb.toString()
            mTvDetail.visibility = View.VISIBLE
            resultBlock?.invoke(result)
        }
    }
}