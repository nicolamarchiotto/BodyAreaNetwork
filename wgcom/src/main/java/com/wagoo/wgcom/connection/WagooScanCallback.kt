package com.wagoo.wgcom.connection

import android.bluetooth.BluetoothDevice

abstract class WagooScanCallback {

    abstract fun onDeviceFound(device: WagooDevice)
    abstract fun onScanComplete(devices: List<WagooDevice>)

    internal var stopHandler: (() -> Unit)? = null
    protected fun stopScan() {
        stopHandler?.invoke()
    }
}