package io.keyss.library.test

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.keyss.library.common.network.NetworkUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        GlobalScope.launch(Dispatchers.IO) {
            /*delay(1_000)
            println("Thread: ${Thread.currentThread().name}")
            val networkTestDialog = NetworkTestDialog()
            networkTestDialog.show(supportFragmentManager, "test ")


            val gpsOpen = NetworkUtil.isGpsOpen(this@MainActivity)
            println("gpsOpen=$gpsOpen")
            if (!gpsOpen) {
                NetworkUtil.openGps(this@MainActivity)
            }*/
            println("NetworkUtil.getInterfaceConfigByApi() start")
            /*NetworkUtil.getDefaultGateway()?.let {
                println(NetworkUtil.getPing(destination = it))
            }*/
            println(NetworkUtil.getInterfaceConfigByApi(false,false))
            println("NetworkUtil.getInterfaceConfigByApi() end")
        }
    }
}