package io.keyss.library.common.broadcast

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.util.Log
import androidx.annotation.MainThread
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent

/**
 * @author Key
 * Time: 2022/03/23 14:11
 * Description:
 */
@RequiresApi(Build.VERSION_CODES.N)
class NetworkReceiver : ConnectivityManager.NetworkCallback, LifecycleObserver {

    constructor() : super()

    constructor(onReceiveListener: ((isAvailable: Boolean) -> Unit)?) : super() {
        this.onReceiveListener = onReceiveListener
    }

    var onReceiveListener: ((isAvailable: Boolean) -> Unit)? = null

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
            registerReceiver(it, onReceiveListener!!)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        mContext?.let {
            unregisterReceiver(it)
        }
    }

    override fun onAvailable(network: Network) {
        super.onAvailable(network)
        onReceiveListener?.invoke(true)
    }

    override fun onLost(network: Network) {
        super.onLost(network)
        onReceiveListener?.invoke(false)
    }

    companion object {
        private const val TAG = "NetworkReceiver"
        private val mNetworkReceivers = mutableMapOf<Int, MutableList<NetworkReceiver>>()
        var debug = false

        /**
         * 可以注册多个，所以请自行管理
         * fixme 目前这样一个activity内的只会存在一个
         * todo 适配fragment
         */
        fun registerReceiver(context: Context, onReceiveListener: ((isAvailable: Boolean) -> Unit)) {
            val connectivityManager = ContextCompat.getSystemService(context, ConnectivityManager::class.java)
            if (null != connectivityManager) {
                val networkReceiver = NetworkReceiver(onReceiveListener)
                // 注册网络监测
                connectivityManager.registerDefaultNetworkCallback(networkReceiver)
                mNetworkReceivers[context.hashCode()]?.also {
                    it.add(networkReceiver)
                } ?: kotlin.run {
                    mNetworkReceivers[context.hashCode()] = mutableListOf(networkReceiver)
                }
            } else {
                Log.e(TAG, "注册网络监听不成功")
            }
            if (debug) {
                Log.i(TAG, "Register NetworkReceiver = ${null != connectivityManager}")
            }
        }

        fun unregisterReceiver(context: Context) {
            val connectivityManager = ContextCompat.getSystemService(context, ConnectivityManager::class.java)
            if (null != connectivityManager) {
                mNetworkReceivers[context.hashCode()]?.let {
                    if (it.isNotEmpty()) {
                        for (networkReceiver in it) {
                            if (debug) {
                                Log.i(TAG, "Unregister Receiver=${it}")
                            }
                            connectivityManager.unregisterNetworkCallback(networkReceiver)
                        }
                    }
                }
            } else {
                Log.e(TAG, "unregister NetworkReceiver: 未能成功获取到连接管理器")
            }
        }

        /**
         * 传this
         * 只有this 和 this as LifecycleOwner 哈希是相等的
         * 但是fragment自己没有context
         */
        @Throws
        fun registerReceiverByLifecycle(context: Context, onReceiveListener: ((isAvailable: Boolean) -> Unit)) {
            // 强转不判断来抛异常解决编译期问题
            val networkReceiver = NetworkReceiver(onReceiveListener)
            networkReceiver.setLifecycleOwner(context)
        }
    }
}