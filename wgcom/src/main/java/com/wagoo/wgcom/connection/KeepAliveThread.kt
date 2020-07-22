package com.wagoo.wgcom.connection

import com.wagoo.wgcom.WagooGlassesInterface
import com.wagoo.wgcom.functions.core_functions.PingMode
import java.lang.ref.WeakReference

internal class KeepAliveThread(private val wagooInterfaceRef: WeakReference<WagooGlassesInterface>) {

    private var aliveThread: Thread? = null

    fun start(force: Boolean = false) {
        if (aliveThread?.isAlive != true) {
            aliveThread = Thread(Runnable {

                var isConnected = true
                while (isConnected || force) {
                    val wagooInterface = wagooInterfaceRef.get() ?: return@Runnable

                    wagooInterface.coreRequest.ping(PingMode.KeepAwake)
                    try {
                        Thread.sleep(3000)
                    } catch (ex: Exception) {

                    }
                    isConnected = wagooInterface.isConnected
                }
            })
            aliveThread!!.start()
        }
    }


}