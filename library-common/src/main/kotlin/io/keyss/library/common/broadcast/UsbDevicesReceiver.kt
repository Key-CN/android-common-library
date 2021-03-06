package io.keyss.library.common.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.util.Log
import androidx.annotation.MainThread
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import io.keyss.library.common.extensions.string

/**
 * 2020/06/18
 */
class UsbDevicesReceiver : BroadcastReceiver, LifecycleObserver {

    constructor() : super()

    constructor(onReceiveListener: ((isAttached: Boolean, device: UsbDevice) -> Unit)?) : super() {
        this.onReceiveListener = onReceiveListener
    }

    var onReceiveListener: ((isAttached: Boolean, device: UsbDevice) -> Unit)? = null

    private var mRegisterKey: Int = 0
    var mLifecycleOwner: LifecycleOwner? = null
        private set
    var mContext: Context? = null

    @MainThread
    fun <T : Context> setLifecycleOwner(context: T) {
        mContext = context
        setLifecycleOwner(context as LifecycleOwner)
    }

    @MainThread
    private fun setLifecycleOwner(lifecycleOwner: LifecycleOwner?) {
        if (mLifecycleOwner === lifecycleOwner) {
            return
        }
        mLifecycleOwner?.lifecycle?.removeObserver(this)
        mLifecycleOwner = lifecycleOwner
        if (lifecycleOwner != null) {
            lifecycleOwner.lifecycle.addObserver(this)
            mRegisterKey = lifecycleOwner.hashCode()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        mContext?.let {
            registerReceiver(it, true, onReceiveListener!!)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        mContext?.let {
            unregisterReceiver(it)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (debug) {
            Log.i(TAG, "usb????????????: Action=${intent.action}, extras=${intent.extras?.string()}")
        }
        val extras = intent.extras
        val action = intent.action
        if (null != extras && null != action) {
            when (action) {
                UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                    (extras[USB_EXTRA_KEY] as? UsbDevice)?.let {
                        if (debug) {
                            Log.i(TAG, "usb????????????: pid=${it.productId}, vid=${it.vendorId}, $it")
                        }
                        onReceiveListener?.invoke(true, it)
                    }
                }
                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    (extras[USB_EXTRA_KEY] as? UsbDevice)?.let {
                        if (debug) {
                            Log.i(TAG, "usb????????????: pid=${it.productId}, vid=${it.vendorId}, $it")
                        }
                        onReceiveListener?.invoke(false, it)
                    }
                }
                else -> Log.w(TAG, "usb????????????: ???????????????Action")
            }
        }
    }

    companion object {
        const val USB_EXTRA_KEY = "device"
        private const val TAG = "UsbDevicesReceiver"
        private val mUsbDevicesReceivers = mutableMapOf<Int, MutableList<UsbDevicesReceiver>>()
        var debug = false

        fun getIntentFilter(): IntentFilter {
            val intentFilter = IntentFilter()
            intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
            return intentFilter
        }

        /**
         * ??????????????????????????????????????????
         * @param isReceiveExistingImmediately ????????????????????????????????????
         * fixme ??????????????????activity????????????????????????
         */
        fun registerReceiver(
            context: Context,
            isReceiveExistingImmediately: Boolean = true,
            onReceiveListener: ((isAttached: Boolean, device: UsbDevice) -> Unit)
        ) {
            val usbDevicesReceiver = UsbDevicesReceiver(onReceiveListener)
            context.registerReceiver(usbDevicesReceiver, getIntentFilter())
            mUsbDevicesReceivers[context.hashCode()]?.also {
                it.add(usbDevicesReceiver)
            } ?: kotlin.run {
                mUsbDevicesReceivers[context.hashCode()] = mutableListOf(usbDevicesReceiver)
            }

            // ??????????????????????????????
            if (isReceiveExistingImmediately) {
                val mUsbManager = ContextCompat.getSystemService(context, UsbManager::class.java)
                mUsbManager?.deviceList?.let { map ->
                    map.forEach {
                        // key???name?????????????????? e.g. /dev/bus/usb/004/003
                        //Log.i(TAG, "???????????????${it.key}: ${it.value}")
                        it.value?.let { usbDevice ->
                            onReceiveListener(true, usbDevice)
                        }
                    }
                }
            }
            if (debug) {
                Log.i(TAG, "Register Receiver: $usbDevicesReceiver")
            }
        }

        fun unregisterReceiver(context: Context) {
            mUsbDevicesReceivers[context.hashCode()]?.let {
                if (it.isNotEmpty()) {
                    for (usbDevicesReceiver in it) {
                        if (debug) {
                            Log.i(TAG, "Unregister Receiver=${it}")
                        }
                        context.unregisterReceiver(usbDevicesReceiver)
                    }
                }
            }
        }

        /**
         * ???this
         * ??????this ??? this as LifecycleOwner ??????????????????
         * ??????fragment????????????context
         */
        @Throws
        fun registerReceiverByLifecycle(context: Context, onReceiveListener: ((isAttached: Boolean, device: UsbDevice) -> Unit)) {
            // ????????????????????????????????????????????????
            val usbDevicesReceiver = UsbDevicesReceiver(onReceiveListener)
            usbDevicesReceiver.setLifecycleOwner(context)
        }
    }
}