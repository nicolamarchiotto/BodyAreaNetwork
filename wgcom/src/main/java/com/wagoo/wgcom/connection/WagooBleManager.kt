package com.wagoo.wgcom.connection

import android.bluetooth.*
import android.content.Context
import android.os.Looper
import android.util.Log
import com.beepiz.bluetooth.gattcoroutines.*
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.scan.ScanSettings
import com.wagoo.wgcom.UuidUtils
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.sendBlocking
import java.lang.Exception
import java.util.*
import kotlin.concurrent.thread
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


enum class BLEStatus {
    DISCONNECTED,
    CONNECTING,
    DISCOVERING_SERVICES,
    CONNECTED_QCC,
    CONNECTED_NORDIC
}

private val QCC_SERVICE_UUID = UUID.fromString("00001100-D102-11E1-9B23-00025B00A5A5")
private val QCC_CHARACTERISTIC_WRITE = UUID.fromString("00001101-D102-11E1-9B23-00025B00A5A5")
private val QCC_CHARACTERISTIC_NOTIFY = UUID.fromString("00001102-D102-11E1-9B23-00025B00A5A5")

private val NORDIC_UART_SERVICE = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E")
private val NORDIC_UART_RX = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E")
private val NORDIC_UART_TX = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E")
private val CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

internal interface WagooBleCallbackDebug {
    fun onConnection()
    fun onMtuSet()
    fun onServiceDiscovered()
    fun onNordicServiceFound()
    fun onReadCharacteristicFound()
    fun onWriteCharacteristicFound()
    fun onConnectionComplete()
    fun onDisconnection()
}


internal class WagooBleManager(val context: Context,
                               device: BluetoothDevice,
                               packetsBufferSize: Int,
                               private val deviceTransport: WagooTransport,
                               internal var debug: WagooBleCallbackDebug? = null) : ConnectionManager(device) {

    val sendQueue: Channel<ByteArray> = Channel(packetsBufferSize)
    var writeCharacteristic: BluetoothGattCharacteristic? = null
    var readCharacteristic: BluetoothGattCharacteristic? = null

    private var currentMtu  = 32

    val coroutineScope = CoroutineScope(Dispatchers.IO)
    var bleCoroutine: Job? = null

    override fun connect(reconnect: Boolean) {

        if (!reconnect && bleCoroutine?.isActive == true) return

        val lastCoroutine = bleCoroutine

        bleCoroutine = coroutineScope.launch {

            val handler = CoroutineExceptionHandler { _, exception ->
                println("CoroutineExceptionHandler got $exception")
            }

            withTimeout(1500) {
                lastCoroutine?.join()
            }
            status = BLEStatus.DISCONNECTED

            val bleConnection = GattConnection(device!!, GattConnection.ConnectionSettings(
                    true,
                    allowAutoConnect = true,
                    disconnectOnClose = true,
                    transport = BluetoothDevice.TRANSPORT_LE
            ))

            var recvCoroutine: Job? = null
            try {

                status = BLEStatus.CONNECTING

                try {
                    withTimeout(5000) {
                        bleConnection.connect()
                        debug?.onConnection()
                    }
                } catch (ex: Exception) {
                    Log.e("WAGOO_BLE", "Cannot connect to glasses!")
                    ex.printStackTrace()
                    return@launch
                }

                try {
                    withTimeout(3000) {
                        currentMtu = bleConnection.requestMtu(232)
                        debug?.onMtuSet()
                    }
                } catch (ex: OperationFailedException) {
                    Log.e("WAGOO_BLE", "Mtu set failed!")
                    if (!isActive) return@launch
                }

                try {
                    withTimeoutOrNull(5000) {

                        val services = bleConnection.discoverServices()
                        debug?.onServiceDiscovered()

                        for (service in services) {
                            if (service.uuid == NORDIC_UART_SERVICE) {

                                debug?.onNordicServiceFound()

                                readCharacteristic = service.getCharacteristic(NORDIC_UART_TX)
                                debug?.onWriteCharacteristicFound()

                                writeCharacteristic = service.getCharacteristic(NORDIC_UART_RX)
                                debug?.onReadCharacteristicFound()

                                break
                            } else if (service.uuid == QCC_SERVICE_UUID) {
                                readCharacteristic = service.getCharacteristic(QCC_CHARACTERISTIC_NOTIFY)
                                writeCharacteristic = service.getCharacteristic(QCC_CHARACTERISTIC_WRITE)
                                break
                            }
                        }
                    }

                    readCharacteristic?.let {
                        bleConnection.setCharacteristicNotificationsEnabledOnRemoteDevice(it, true)
                        recvCoroutine = coroutineScope.launch {
                            var subscriber: ReceiveChannel<BGC>? = null
                            try {
                                subscriber = bleConnection.openNotificationSubscription(it)
                                for (notification in subscriber) {
                                    onReceiveData(notification.value)
                                }
                            }
                            catch (_: ConnectionClosedException) { }
                            catch (_: OperationInitiationFailedException) { }
                            finally {
                                subscriber?.cancel()
                            }
                        }
                    }

                    status = BLEStatus.CONNECTED_NORDIC
                    debug?.onConnectionComplete()


                    writeCharacteristic?.let {
                        while (true) {
                            val buffer = sendQueue.receive()
                            it.value = buffer
                            withTimeoutOrNull(5000) {
                                try {
                                    bleConnection.writeCharacteristic(it)
                                }catch (_: CancellationException) {
                                    return@withTimeoutOrNull null
                                }
                                return@withTimeoutOrNull Unit
                            } ?: break
                        }
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
            finally {
                val isActive = isActive
                coroutineScope.launch {
                    try {
                        recvCoroutine?.cancel()
                        recvCoroutine?.join()
                    } finally {
                        delay(500)
                        bleConnection.close(false)

                        if (isActive) {
                            connect(true)
                            this.cancel()
                        }
                        status = BLEStatus.DISCONNECTED
                    }
                }.join()
            }
        }
    }

    override fun disconnect() {
        if (isConnected() || isConnecting()) {

            coroutineScope.launch {
                try {
                    bleCoroutine?.cancel()
                    status = BLEStatus.DISCONNECTED
                }
                catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }
    }

    override fun isConnected(): Boolean {
        return status == BLEStatus.CONNECTED_QCC || status == BLEStatus.CONNECTED_NORDIC
    }

    fun isConnecting(): Boolean {
        return status == BLEStatus.CONNECTING || status == BLEStatus.DISCOVERING_SERVICES
    }

    override fun getMtu(): Int {
        return currentMtu
    }

    override fun getTransport(): WagooTransport {
        return deviceTransport
    }

    override fun getMinimumSendPeriodMs(): Long {
        return 0
    }

    override fun sendData(data: ByteArray): Boolean {
        sendQueue.sendBlocking(data)
        return true
    }

    override fun requireKeepAlive(): Boolean {
        return true
    }

    var status = BLEStatus.DISCONNECTED
        private set(value) {
            if (field != value) {
                field = value
                when (value) {
                    BLEStatus.DISCONNECTED -> onDisconnect()
                    BLEStatus.CONNECTING -> onConnecting()
                    BLEStatus.CONNECTED_NORDIC,
                    BLEStatus.CONNECTED_QCC -> {
                        // Run on thread because ble does not work on callbacks!
                        thread {
                            Looper.prepare()
                            onConnect()
                        }
                    }
                    BLEStatus.DISCOVERING_SERVICES -> {
                    }
                }
            }
        }

//                gatt?.setCharacteristicNotification(manager.readCharacteristic, true)
//                val descriptor = manager.readCharacteristic?.getDescriptor(CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID)
//                descriptor?.value = ENABLE_NOTIFICATION_VALUE
//                gatt?.writeDescriptor(descriptor)

    companion object {

        private lateinit var rxBleClient: RxBleClient

        suspend fun scanBleWagooDevices(context: Context): Channel<WagooDevice> = suspendCoroutine {

            rxBleClient = RxBleClient.create(context)

            val channel = Channel<WagooDevice>(Channel.UNLIMITED)

            val scanObject = rxBleClient.scanBleDevices(ScanSettings.Builder()
                    .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                    .build()).subscribe({
                it

                val device = it.bleDevice ?: return@subscribe

                val deviceName = device.name?.toLowerCase(Locale.ROOT) ?: return@subscribe

                val transport = when {
                    deviceName.contains("wagoohpglasses") -> WagooTransport.BleQcc
                    deviceName.contains("wagooglassesble") -> WagooTransport.BleNordic
                    else -> WagooTransport.Unknown
                }

                if (transport != WagooTransport.Unknown) {
                    val wagooDevice = WagooDevice(
                            device.bluetoothDevice,
                            WagooBleManager(context, device.bluetoothDevice, 8, transport),
                            it.scanRecord?.bytes?.let {
                                val uuidStart = 8 + 6
                                val uuidEnd = uuidStart + 16
                                if (it.size < uuidEnd) return@let null
                                return@let UuidUtils.asUuid(it.sliceArray(uuidStart until uuidEnd))
                            },
                            device.macAddress
                    )
                    try {
                        channel.sendBlocking(wagooDevice)
                    } catch (ex: ClosedSendChannelException) {
                    }
                }
            }, {
                Log.e("WAGOO_BLE", "Error!")
            })

            channel.invokeOnClose {
                scanObject.dispose()
            }

            Log.d("BLEMANAGER", "Scan started!")

            it.resume(channel)
        }
    }
}