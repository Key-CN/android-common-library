package io.keyss.library.common.network

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import io.keyss.library.common.R

/**
 * @author Key
 * Time: 2021/07/07 17:59
 * Description:
 */
class NetworkTestFragment : Fragment() {
    lateinit var mButton: AppCompatButton


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mButton = view.findViewById<AppCompatButton>(R.id.b_test_network_fragment)
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
            }
            getString(R.string.stop_test) -> {
                button.setText(R.string.start_test)
            }
            else -> {
                Toast.makeText(context, "程序错误", Toast.LENGTH_LONG).show()
            }
        }
    }
}