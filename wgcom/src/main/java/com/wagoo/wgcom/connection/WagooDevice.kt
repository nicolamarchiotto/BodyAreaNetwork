package com.wagoo.wgcom.connection

import android.bluetooth.BluetoothDevice
import android.net.MacAddress
import java.util.*

class WagooDevice internal constructor(
        val device: BluetoothDevice?,
        internal val connectionManager: ConnectionManager,
        val uuid: UUID?,
        val mac: String?
    ) {
}