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
            Log.i(TAG, "usb状态变化: Action=${intent.action}, extras=${intent.extras?.string()}")
        }
        val extras = intent.extras
        val action = intent.action
        if (null != extras && null != action) {
            when (action) {
                UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                    (extras[USB_EXTRA_KEY] as? UsbDevice)?.let {
                        if (debug) {
                            Log.i(TAG, "usb设备插入: pid=${it.productId}, vid=${it.vendorId}, $it")
                        }
                        onReceiveListener?.invoke(true, it)
                    }
                }
                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    (extras[USB_EXTRA_KEY] as? UsbDevice)?.let {
                        if (debug) {
                            Log.i(TAG, "usb设备移除: pid=${it.productId}, vid=${it.vendorId}, $it")
                        }
                        onReceiveListener?.invoke(false, it)
                    }
                }
                else -> Log.w(TAG, "usb状态变化: 未能判定该Action")
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
         * 可以注册多个，所以请自行管理
         * @param isReceiveExistingImmediately 是否立刻返回已存在的设备
         * fixme 目前这样一个activity内的只会存在一个
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

            // 立刻返回已存在的设备
            if (isReceiveExistingImmediately) {
                val mUsbManager = ContextCompat.getSystemService(context, UsbManager::class.java)
                mUsbManager?.deviceList?.let { map ->
                    map.forEach {
                        // key是name，也就是节点 e.g. /dev/bus/usb/004/003
                        //Log.i(TAG, "已接入的：${it.key}: ${it.value}")
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
         * 传this
         * 只有this 和 this as LifecycleOwner 哈希是相等的
         * 但是fragment自己没有context
         */
        @Throws
        fun registerReceiverByLifecycle(context: Context, onReceiveListener: ((isAttached: Boolean, device: UsbDevice) -> Unit)) {
            // 强转不判断来抛异常解决编译期问题
            val usbDevicesReceiver = UsbDevicesReceiver(onReceiveListener)
            usbDevicesReceiver.setLifecycleOwner(context)
        }
    }
}