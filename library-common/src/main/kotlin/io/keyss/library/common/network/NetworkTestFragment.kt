package io.keyss.library.common.network

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import io.keyss.library.common.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * @author Key
 * Time: 2021/07/07 17:59
 * Description:
 */
class NetworkTestFragment : Fragment() {
    lateinit var mButton: AppCompatButton
    lateinit var mProgressBar: ProgressBar

    private var mTestJob: Job? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mButton = view.findViewById<AppCompatButton>(R.id.b_test_network_fragment)
        mProgressBar = view.findViewById<ProgressBar>(R.id.pb_test_network_fragment)
        mButton.setOnClickListener {
            clickButton(it as AppCompatButton)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_network_test, container, false)
    }

    private fun clickButton(button: AppCompatButton): Unit {
        when (button.text) {
            getString(R.string.start_test) -> {
                button.setText(R.string.stop_test)
                startJob()
            }
            getString(R.string.stop_test) -> {
                cancelJob()
            }
            else -> {
                Toast.makeText(context, "程序错误", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun startJob() {
        cancelJob()
        println("startJob")
        mProgressBar.visibility = View.VISIBLE
        mTestJob = lifecycleScope.launch(Dispatchers.IO) {
            val executeTesting = NetworkUtil.executeTesting("192.168.101.4")
            println(executeTesting)
            cancelJob()
        }
    }

    private fun cancelJob() {
        println("cancelJob")
        mButton.setText(R.string.start_test)
        mProgressBar.visibility = View.GONE
        mTestJob?.let {
            if (it.isActive) {
                it.cancel()
            }
        }
        mTestJob = null
    }
}