package com.wagoo.wgcom.connection

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Looper
import android.os.ParcelUuid
import java.util.*
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

enum class BluetoothStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED
}

private val SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
private val GAIA = UUID.fromString("00001107-D102-11E1-9B23-00025B00A5A5")

internal class WagooBluetoothManager(device: BluetoothDevice) : ConnectionManager(device) {

    private var connectionThread: Thread? = null
    private var communicationThread: Thread? = null
    private var userStatusRequest: BluetoothStatus = BluetoothStatus.DISCONNECTED

    override fun connect(reconnect: Boolean) {
        if (!reconnect) userStatusRequest = BluetoothStatus.CONNECTED
        if (!isConnected() && !isConnecting()) {
            connectInternal(null)
        }
    }

    override fun disconnect() {
        userStatusRequest = BluetoothStatus.DISCONNECTED
        if (isConnected() || isConnecting()) {
            status = BluetoothStatus.DISCONNECTED
            socket?.close()
        }
    }

    override fun isConnected(): Boolean {
        return status == BluetoothStatus.CONNECTED
    }

    fun isConnecting(): Boolean {
        return status == BluetoothStatus.CONNECTING
    }

    override fun getMtu(): Int {
        return 232
    }

    override fun getTransport(): WagooTransport {
        return WagooTransport.RfcommQcc
    }

    override fun getMinimumSendPeriodMs(): Long {
        return 20
    }

    override fun sendData(data: ByteArray): Boolean {
        try {
            socket?.outputStream?.let {
                it.write(data)
                it.flush()
                return true
            }
            return false
        }
        catch (ex: Exception) {
            ex.printStackTrace()
            status = BluetoothStatus.DISCONNECTED
            if (userStatusRequest != BluetoothStatus.DISCONNECTED)
                asyncReconnect(1000)
            return false
        }
    }

    override fun requireKeepAlive(): Boolean {
        return true
    }

    private var socket: BluetoothSocket? = null
    private fun selectUuid(uuids: Array<ParcelUuid>): UUID? {
        for (parcel in uuids) {
            if (parcel.uuid == SPP || parcel.uuid == GAIA) {
                return parcel.uuid
            }
        }
        return null
    }

    private var status = BluetoothStatus.DISCONNECTED
    private set(value) {
        if (field != value) {
            field = value

            when (value) {
                BluetoothStatus.DISCONNECTED -> onDisconnect()
                BluetoothStatus.CONNECTING -> onConnecting()
                BluetoothStatus.CONNECTED -> {
                    onConnect()
                }
            }
        }
    }

    private fun listen(socket: BluetoothSocket) {

        if (status == BluetoothStatus.DISCONNECTED) {
            socket.close()
            return
        }

        communicationThread?.interrupt()

        var connectionLost = false

        communicationThread = thread {
            Looper.prepare()
            try {
                status = BluetoothStatus.CONNECTED
                inputLoop@ while (status == BluetoothStatus.CONNECTED) {
                    val bytes = ByteArray(2048)
                    when (val result = socket.inputStream.read(bytes)) {
                        0 -> continue@inputLoop
                        -1 -> {
                            if (status == BluetoothStatus.CONNECTED) {
                                connectionLost = true
                            }
                            status = BluetoothStatus.DISCONNECTED
                            socket.close()
                            continue@inputLoop
                        }
                        else -> {
                            onReceiveData(bytes.sliceArray(0 until result))
                        }
                    }
                }
            }
            catch (ex: Exception) {
                ex.printStackTrace()
            }
            finally {
                try {
                    socket.close()
                    status = BluetoothStatus.DISCONNECTED
                    // Try to reconnect!
                    if (connectionLost && (userStatusRequest != BluetoothStatus.DISCONNECTED)) {
                        asyncReconnect(1000)
                    }
                }
                catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }
    }

    private fun connectInternal(context: Context?) {
        val device = device ?: return
        status = BluetoothStatus.CONNECTING
        connectionThread = thread {
            Looper.prepare()
            try {
                device.fetchUuidsWithSdp()

                if (context != null) {
                    val lock = Semaphore(1)
                    lock.acquire()
                    context.registerReceiver(object : BroadcastReceiver() {
                        override fun onReceive(context: Context?, intent: Intent?) {
                            lock.release()
                        }
                    }, IntentFilter(BluetoothDevice.ACTION_UUID))
                    lock.tryAcquire(10, TimeUnit.SECONDS)
                }

                selectUuid(device.uuids)?.let { uuid ->
                    socket = device.createInsecureRfcommSocketToServiceRecord(uuid)
                    socket?.let {
                        it.connect()
                        listen(it)
                        return@thread
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
            status = BluetoothStatus.DISCONNECTED
            if (userStatusRequest != BluetoothStatus.DISCONNECTED)
                asyncReconnect(2000)
        }
    }

    companion object {
        fun getBondedWagooDevices(wagooScanCallback: WagooScanCallback) {
            thread {
                Looper.prepare()
                val adapter = BluetoothAdapter.getDefaultAdapter()
                //        adapter.startDiscovery()
                var stopScan = false
                wagooScanCallback.stopHandler = {
                    stopScan = true
                }

                val devices = mutableListOf<WagooDevice>()
                for (device in adapter.bondedDevices) {
                    if (device.name.toLowerCase().contains("wagoohpglasses")) {
                        if (!stopScan) {

                            val wagooDevice = WagooDevice(
                                    device,
                                    WagooBluetoothManager(device),
                                    null,
                                    device.address
                            )

                            wagooScanCallback.onDeviceFound(wagooDevice)
                            devices.add(wagooDevice)
                        } else break
                    }
                }
                wagooScanCallback.onScanComplete(devices)
            }
        }
    }

}