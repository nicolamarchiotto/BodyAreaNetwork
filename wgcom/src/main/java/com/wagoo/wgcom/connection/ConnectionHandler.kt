package com.wagoo.wgcom.connection

abstract class ConnectionHandler {
    abstract fun onConnected()
    abstract fun onConnecting()
    abstract fun onDisconnected()
}