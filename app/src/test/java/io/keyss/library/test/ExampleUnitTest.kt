package io.keyss.library.test

import io.keyss.library.common.utils.HashUtil
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun hashTest() {
        val digest = HashUtil.getFileHash("/Users/key/Downloads/balenaEtcher-1.5.120.dmg", HashUtil.AlgorithmType.MD5)
        println("长度=${digest.length}, digest=${digest}")
    }

    @Test
    fun aaTest() {
        val str = """
lo        Link encap:Local Loopback
          inet addr:127.0.0.1  Mask:255.0.0.0
          inet6 addr: ::1/128 Scope: Host
          UP LOOPBACK RUNNING  MTU:65536  Metric:1
          RX packets:26331 errors:0 dropped:0 overruns:0 frame:0
          TX packets:26331 errors:0 dropped:0 overruns:0 carrier:0
          collisions:0 txqueuelen:1
          RX bytes:385640079 TX bytes:385640079

wlan0     Link encap:Ethernet  HWaddr cc:4b:73:a8:f2:68
          inet addr:192.168.101.3  Bcast:192.168.101.255  Mask:255.255.255.0
          inet6 addr: fe80::ce4b:73ff:fea8:f268/64 Scope: Link
          UP BROADCAST RUNNING MULTICAST  MTU:1500  Metric:1
          RX packets:1054221 errors:0 dropped:1 overruns:0 frame:0
          TX packets:4878473 errors:0 dropped:0 overruns:0 carrier:0
          collisions:0 txqueuelen:1000
          RX bytes:150630956 TX bytes:347318867

eth0      Link encap:Ethernet  HWaddr 1a:1e:38:75:fb:19
          inet6 addr: fe80::181e:38ff:fe75:fb19/64 Scope: Link
          UP BROADCAST RUNNING MULTICAST  MTU:1500  Metric:1
          RX packets:0 errors:0 dropped:0 overruns:0 frame:0
          TX packets:6228 errors:0 dropped:0 overruns:0 carrier:0
          collisions:0 txqueuelen:1000
          RX bytes:0 TX bytes:2158484
          Interrupt:24
        """.trimIndent()



    }
}