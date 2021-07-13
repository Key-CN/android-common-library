package io.keyss.library.common.network

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import io.keyss.library.common.R

/**
 * @author Key
 * Time: 2021/07/07 17:39
 * Description:
 */
class NetworkTestDialog : DialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_network_test, container, false)
        return view
    }
}