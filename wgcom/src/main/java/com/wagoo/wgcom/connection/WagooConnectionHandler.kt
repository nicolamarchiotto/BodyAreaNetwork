package com.wagoo.wgcom.connection

import com.wagoo.wgcom.WagooGlassesInterface

abstract class WagooConnectionHandler {
    abstract fun onDeviceFound(wagooInterface: WagooGlassesInterface, device: WagooDevice)
    abstract fun onConnected(wagooInterface: WagooGlassesInterface)
    abstract fun onConnecting(wagooInterface: WagooGlassesInterface)
    abstract fun onDisconnected(wagooInterface: WagooGlassesInterface)

    internal fun toConnectionCallback(wagooInterface: WagooGlassesInterface): ConnectionHandler {
        return object : ConnectionHandler() {
            override fun onConnected() { this@WagooConnectionHandler.onConnected(wagooInterface) }
            override fun onConnecting() { this@WagooConnectionHandler.onConnecting(wagooInterface) }
            override fun onDisconnected() { this@WagooConnectionHandler.onDisconnected(wagooInterface) }
        }
    }
}