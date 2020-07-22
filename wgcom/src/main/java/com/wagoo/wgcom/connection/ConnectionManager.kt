package com.wagoo.wgcom.connection

import android.bluetooth.BluetoothDevice
import kotlin.concurrent.thread

abstract class ConnectionManager(protected val device: BluetoothDevice?) {

    /// Connect or reconnect to the device
    protected abstract fun connect(reconnect: Boolean)

    /// Connect the device
    fun connect() {
        connect(false)
    }

    /// Disconnect the device
    abstract fun disconnect()

    /// Check if the device is connected
    abstract fun isConnected(): Boolean

    /// Get the current mtu
    abstract fun getMtu(): Int

    /// Get the current transport
    abstract fun getTransport(): WagooTransport

    /// Get minimum time between consecutive packets
    abstract fun getMinimumSendPeriodMs(): Long

    /// Sends data to the device
    abstract fun sendData(data: ByteArray): Boolean

    /// True if the channel requires to be kept alive
    abstract fun requireKeepAlive(): Boolean

    private val connectionStatusCallbacks = mutableListOf<ConnectionHandler>()
    private var dataCallback: ((ByteArray) -> Unit)? = null
    private var doReconnect = true

    /// Registers a data callback (only one allowed!)
    fun registerDataCallback(callback: (ByteArray) -> Unit) {
        dataCallback = callback
    }

    protected fun onReceiveData(data: ByteArray) {
        dataCallback?.invoke(data)
    }

    protected fun onConnect() {
        for (callback in connectionStatusCallbacks) {
            callback.onConnected()
        }
    }

    protected fun onConnecting() {
        for (callback in connectionStatusCallbacks) {
            callback.onConnecting()
        }
    }

    protected fun onDisconnect() {
        for (callback in connectionStatusCallbacks) {
            callback.onDisconnected()
        }
    }

    fun setReconnection(enabled: Boolean) {
        doReconnect = enabled
    }

    protected fun asyncReconnect(timeout: Long) {
        if (doReconnect) {
            thread {
                Thread.sleep(timeout)
                connect(true)
            }
        }
    }

    fun registerConnectionStatusCallback(connectionHandler: ConnectionHandler) {
        if (!connectionStatusCallbacks.contains(connectionHandler))
            connectionStatusCallbacks.add(connectionHandler)
    }

    fun unregisterConnectionStatusCallback(connectionHandler: ConnectionHandler) {
        connectionStatusCallbacks.remove(connectionHandler)
    }
}