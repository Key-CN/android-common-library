package io.keyss.library.test

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.keyss.library.common.network.SimpleNetworkTestDialog
import kotlinx.coroutines.delay

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        lifecycleScope.launchWhenResumed {
            delay(1_000)
            println("Thread: ${Thread.currentThread().name}")
            val d = SimpleNetworkTestDialog("192.168.101.2")
            d.show(supportFragmentManager, "test")

            /*val gpsOpen = NetworkUtil.isGpsOpen(this@MainActivity)
            println("gpsOpen=$gpsOpen")
            if (!gpsOpen) {
                NetworkUtil.openGps(this@MainActivity)
            }*/
        }
        /*lifecycleScope.launch(Dispatchers.IO) {
            println("NetworkUtil.getInterfaceConfigByApi() start")
            *//*NetworkUtil.getDefaultGateway()?.let {
                println(NetworkUtil.getPing(destination = it))
            }*//*

            println(NetworkUtil.getActiveInterfaceConfig(this@MainActivity))
            println("NetworkUtil.getInterfaceConfigByApi() end")
        }*/
    }
}