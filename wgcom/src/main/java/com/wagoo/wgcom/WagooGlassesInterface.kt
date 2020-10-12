package com.wagoo.wgcom

import androidx.appcompat.app.AppCompatActivity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.wagoo.wgcom.activities.UpdateActivity
import com.wagoo.wgcom.connection.*
import com.wagoo.wgcom.connection.WagooBluetoothManager
import com.wagoo.wgcom.functions.MainRustInterop
import com.wagoo.wgcom.functions.base_functions.*
import com.wagoo.wgcom.functions.base_functions.BaseRequestImpl
import com.wagoo.wgcom.functions.base_functions.BaseResponseImpl
import com.wagoo.wgcom.functions.core_functions.CoreRequestImpl
import com.wagoo.wgcom.functions.core_functions.CoreResponseImpl
import com.wagoo.wgcom.functions.core_functions.PingMode
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Exception
import java.lang.ref.WeakReference
import java.net.URL
import java.util.concurrent.Semaphore
import kotlin.concurrent.thread

private const val REBOOT_CONSTANT = 0x3EB007

data class FirmwareVersion(val major: Int, val minor: Int, val patch: Int) {
    companion object {
        fun fromList(version: List<Int>): FirmwareVersion {
            return FirmwareVersion(version[0], version[1], version[2])
        }
    }
}

class WagooGlassesInterface internal constructor(
        device: WagooDevice?,
        private val connectionHandler: WagooConnectionHandler? = null, _x: Unit) {

    constructor(device: WagooDevice, connectionHandler: WagooConnectionHandler? = null)
            : this(device, connectionHandler, Unit)


    private val sendSemaphore = Semaphore(1)

    private val widQueue = MutableList(256) { it }
    internal var firmwareVersion = mutableListOf<FirmwareVersion?>(null, null, null)

    private val mainInterop = MainRustInterop {
        sendPacketToGlasses(it)
    }

    internal val coreRequest = CoreRequestImpl(this).apply { mainInterop.registerSender(this) }
    private val coreResponse = CoreResponseImpl(this).apply { mainInterop.addReceiver(this) }

    private val baseRequest = BaseRequestImpl(this).apply { mainInterop.registerSender(this) }
    private val baseResponse = BaseResponseImpl(this).apply { mainInterop.addReceiver(this) }

    private var connectionManager: ConnectionManager? = null
    private val keepAliveThread = KeepAliveThread(WeakReference(this))
    private var versionThread: Thread? = null

    internal val updater = Updater(coreRequest)


    internal fun setDevice(device: WagooDevice) {

        if (this.connectionManager != null) return

        val connectionManager = device.connectionManager

        connectionManager.registerConnectionStatusCallback(object : ConnectionHandler() {
            override fun onConnected() {
                if (connectionManager.requireKeepAlive()) {
                    keepAliveThread.start()
                }
                // TODO: Check version!
                if (connectionManager.getTransport() == WagooTransport.BleQcc) {
                    Thread.sleep(500)
                }
                val mtu = connectionManager.getMtu()
                mainInterop.setMtu(mtu)
                if (mtu != 232) {
                    updater.blockSize = mtu - 36
                }
//                kotlinToNordic.disable_logging()
//                dataCollector.onConnected()
            }
            override fun onConnecting() {}
            override fun onDisconnected() {
                dataCollector.onDisconnected()
            }
        })

        connectionHandler?.let {
            connectionManager.registerConnectionStatusCallback(it.toConnectionCallback(this))
        }
        connectionManager.registerDataCallback { processData(it) }

        this.connectionManager = connectionManager

        // Call always at the end of the constructor!
        connectionHandler?.onDeviceFound(this, device)
    }

    init {
        if (device != null) {
            setDevice(device)
        }
    }

    val isConnected: Boolean
    get() {
        return connectionManager?.isConnected() ?: false
    }

    fun connect(): Boolean {
        return connectionManager?.connect() != null
    }

    fun disconnect(): Boolean {
        return connectionManager?.disconnect() != null
    }

    fun setLogging(enabled: Boolean) {
        if (enabled) {
            coreRequest.enable_logging()
        }
        else {
            coreRequest.disable_logging()
        }
    }

    internal fun sendPacketToGlasses(packet: ByteArray) {

        sendSemaphore.acquire()
        try {
            val connectionManager = connectionManager ?: return
            val transportPacket = when (connectionManager.getTransport()) {
                WagooTransport.RfcommQcc -> {
                    val gaiaPacket = byteArrayOf(
                            -1,                     // 255
                            1,                      // version
                            0,                      // flags
                            packet.size.toByte(),   // length
                            0,
                            0xb,                    // vendor id (Wagoo)
                            0x0,
                            0x10,                   // command id (not used)
                            *packet
                    )
                    gaiaPacket
                }
                WagooTransport.BleQcc -> {
                    val gaiaPacket = byteArrayOf(
                            0,
                            0xb,                    // vendor id (Wagoo)
                            0x0,
                            0x10,                   // command id (not used)
                            *packet
                    )
                    gaiaPacket
                }
                WagooTransport.BleNordic,
                WagooTransport.Unknown -> {
                    packet
                }
            }
            connectionManager.sendData(transportPacket)
            Thread.sleep(connectionManager.getMinimumSendPeriodMs())
        } finally {
            sendSemaphore.release()
        }
    }

    private fun processData(data: ByteArray) {
        mainInterop.processData(
            when (connectionManager?.getTransport()) {
                WagooTransport.RfcommQcc -> {
                    if (data.size < 9 + 12) return
                    data.sliceArray(9 until data.size)
                }
                WagooTransport.BleQcc -> {
                    if (data.size < 5 + 12) return
                    data.sliceArray(5 until data.size)
                }
                WagooTransport.Unknown,
                WagooTransport.BleNordic -> {
                    data
                }
                else -> return
            }
        )
    }

    fun firmware_update_from_data(master: ByteArray?, slave: ByteArray?, progress: (Int, Int) -> Unit) {
        updater.start_firmware_update(master, slave, progress)
        firmwareVersion = mutableListOf(null, null, null)
    }

    fun firmware_update_from_embedded_resources(context: Context, master: Boolean, slave: Boolean, progress: (Int, Int) -> Unit) {
        updater.start_firmware_update(context, master, slave, progress)
        firmwareVersion = mutableListOf(null, null, null)
    }

    fun bootup() {
        coreRequest.bootup_glasses(byteArrayOf())
    }

    @JvmOverloads
    fun check_for_updates(context: Context, url: String? = null) {

        url ?: return

        thread {
            try {
                var updateSlave = false
                var updateMaster = false

                val version_master_str = URL("$url/version_master").readText().trim()
                val version_master = version_master_str.split("_").map { it.toInt() }

                val version_slave_str = URL("$url/version_slave").readText().trim()
                val version_slave = version_slave_str.split("_").map { it.toInt() }

                wait_for_chip_version()

                if (firmwareVersion[0] != FirmwareVersion.fromList(version_master)) {
                    updateMaster = true
                }
                if (firmwareVersion[1] != FirmwareVersion.fromList(version_slave)) {
                    updateSlave = true
                }
                if (firmwareVersion[2] != FirmwareVersion.fromList(version_slave)) {
                    updateSlave = true
                }

                if (updateMaster || updateSlave) {
                    val intent = Intent(context, UpdateActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME
                    startActivity(context, intent, null)
                }

            } catch (ex: Exception) {

            }
        }
    }

    private fun wait_for_chip_version() {
        request_version()
        while (firmwareVersion.any { it == null }) Thread.sleep(1000)
        stop_request_version()
    }

    fun firmware_update_online(activity: AppCompatActivity?, url: String, progress: (Int, Int, Boolean) -> Unit) {
        thread {
            try {
                val version_master_str = URL("$url/version_master").readText().trim()
                val version_master = version_master_str.split("_").map { it.toInt() }

                wait_for_chip_version()

                var updateMaster = false
                if (firmwareVersion[0] != FirmwareVersion.fromList(version_master)) {
                    updateMaster = true
                }

                var masterFirmware: ByteArray? = null
                if (updateMaster) {
                    val major = version_master[0]
                    val minor = version_master[1]
                    val patch = version_master[2]
                    activity?.let {
                        activity.runOnUiThread {
                            Toast.makeText(it, "Downloading master firmware version $major.$minor.$patch", Toast.LENGTH_SHORT).show()
                        }
                        Thread.sleep(1000)
                    }

                    masterFirmware = URL(
                            "$url/master_${version_master_str}"
                    ).readBytes()
                }
                else {
                    progress(0, 100, false)
                    coreRequest.ping(PingMode.MasterVersion)

                    activity?.let {
                        it.runOnUiThread {
                            Toast.makeText(it, "Master version: ${version_master[0]}.${version_master[1]}.${version_master[2]}", Toast.LENGTH_SHORT).show()
                        }
                        Thread.sleep(1000)
                    }
                }

                var updateSlave = false
                val version_slave_str = URL("$url/version_slave").readText().trim()
                val version_slave = version_slave_str.split("_").map { it.toInt() }

                if (firmwareVersion[1] != FirmwareVersion.fromList(version_slave)) {
                    updateSlave = true
                }
                if (firmwareVersion[2] != FirmwareVersion.fromList(version_slave)) {
                    updateSlave = true
                }

                firmwareVersion = mutableListOf(null, null, null)

                var slaveFirmware: ByteArray? = null
                if (updateSlave) {
                    val major = version_slave[0]
                    val minor = version_slave[1]
                    val patch = version_slave[2]
                    activity?.let {
                        it.runOnUiThread {
                            Toast.makeText(it, "Downloading slave firmware version $major.$minor.$patch", Toast.LENGTH_SHORT).show()
                        }
                        Thread.sleep(1000)
                    }

                    slaveFirmware = URL(
                            "$url/slave_${version_slave_str}"
                    ).readBytes()
                }
                else {
                    progress(1, 100, false)
                    progress(2, 100, false)
                    activity?.let {
                        it.runOnUiThread {
                            Toast.makeText(it, "Slave version: ${version_slave[0]}.${version_slave[1]}.${version_slave[2]}", Toast.LENGTH_SHORT).show()
                        }
                        Thread.sleep(1000)
                    }
                    coreRequest.ping(PingMode.RightVersion)
                    coreRequest.ping(PingMode.LeftVersion)
                }
                if (masterFirmware != null || slaveFirmware != null) {
                    updater.start_firmware_update(masterFirmware, slaveFirmware) {
                        chip, progress ->
                        progress(chip, progress, true)
                    }
                }
                on_ping_update()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    private var requestVersion = false

    internal fun stop_request_version() {
        requestVersion = false
        versionThread?.interrupt()
    }

    internal fun request_version() {
        requestVersion = true
        if (versionThread?.isAlive != true) {
            versionThread = thread {
                try {
                    while (requestVersion) {//(firmware_version.count { it == null } > 0) {
                        for ((index, version) in firmwareVersion.iterator().withIndex()) {
                            if (version == null) {
                                coreRequest.ping(PingMode.fromValue(index)!!)
                            }
                        }
                        Thread.sleep(1500)
                    }
                }
                catch (ex: Exception) {

                }
            }
        }
    }

    fun ping_glasses() {
        coreRequest.ping(PingMode.MasterVersion)
        coreRequest.ping(PingMode.RightVersion)
        coreRequest.ping(PingMode.LeftVersion)
    }

    fun set_lights(intensity: Float, blink_time_ms: Int?, red: Boolean, yellow: Boolean, green: Boolean) {

        val config = LightsConfig(
                LightMode((intensity * 255).toInt(),
                        blink_time_ms ?: 0),
                red,
                yellow,
                green)

        baseRequest.set_lights(config, config)
    }

    fun set_both_lights(right_intensity: Float,
                        right_blink_time_ms: Int?,
                        right_red: Boolean,
                        right_yellow: Boolean,
                        right_green: Boolean,

                        left_intensity: Float,
                        left_blink_time_ms: Int?,
                        left_red: Boolean,
                        left_yellow: Boolean,
                        left_green: Boolean)
    {

        val rightConfig = LightsConfig(
                LightMode((right_intensity * 255).toInt(),
                        right_blink_time_ms ?: 0),
                right_red,
                right_yellow,
                right_green)

        val leftConfig = LightsConfig(
                LightMode((left_intensity * 255).toInt(),
                        left_blink_time_ms ?: 0),
                left_red,
                left_yellow,
                left_green)

        baseRequest.set_lights(rightConfig, leftConfig)
    }

    fun enable_collect_mode() {
        baseRequest.enable_collect_mode()
    }

    fun disable_collect_mode() {
        baseRequest.disable_collect_mode()
    }

    fun enable_sensors() {
//        baseRequest.set_sensors_state(
//
//
//        )
    }

    fun disable_sensors() {
//        baseRequest.disable_sensors_gathering()
    }

    private fun getNextWid(): Int? {
        val wid = widQueue.getOrNull(0) ?: return null
        widQueue.removeAt(0)
        // FIXME: Workaround for memory overflow on nordic with the new algorithm!
        return wid % 8
    }

    fun whitelist_add_mac(mac: ByteArray): Int? {
        if (mac.size != 6) return null
        val wid = getNextWid() ?: return null

        baseRequest.whitelist_add_mac(wid.toByte(), mac)
        return wid
    }

    fun whitelist_add_uuid(uuid: ByteArray): Int? {
        if (uuid.size != 16) return null
        val wid = getNextWid() ?: return null

        baseRequest.whitelist_add_uuid(wid.toByte(), uuid)
        return wid
    }

    fun whitelist_delete_wid(wid: Int) {
        if (widQueue.contains(wid)) return
        baseRequest.whitelist_remove(wid.toByte())
        widQueue.add(wid)
    }

    fun whitelist_clear() {
        baseRequest.whitelist_clear_all()
    }

    private val sensorsCallbacks = WagooHandlerClass<((AccelGyroInfo) -> Unit)>()
    private val beaconsCallbacks = WagooHandlerClass<((BeaconLight) -> Unit)>()
    private val directionCallbacks = WagooHandlerClass<((Int, Int, Int, Float, Long) -> Unit)>()
    private val doubleTapCallbacks = WagooHandlerClass<((Long) -> Unit)>()
    private val pingCallbacks = WagooHandlerClass<((MutableList<FirmwareVersion?>) -> Unit)>()

    internal fun on_sensors_data(data: AccelGyroInfo) {
        sensorsCallbacks.execute { it.invoke(data) }
    }

    internal fun on_beacons_data(data: BeaconLight) {
        beaconsCallbacks.execute { it.invoke(data) }
    }

    internal fun on_direction_data(wid: Int, direction: Int, progress: Int, findex: Float, ms_since_last: Long) {
        directionCallbacks.execute { it.invoke(wid, direction, progress, findex, ms_since_last) }
    }

    internal fun on_double_tap(timestamp_us: Long) {
        doubleTapCallbacks.execute { it.invoke(timestamp_us) }
    }

    internal fun on_ping_update() {
        pingCallbacks.execute { it.invoke(firmwareVersion) }
    }

    fun reboot(chip: Int, delay: Int) {
        firmwareVersion[chip] =  null
        coreRequest.reboot(chip.toByte(), delay, REBOOT_CONSTANT)
    }

    fun register_collect_sensors_callback(callback: (AccelGyroInfo) -> Unit) {
        sensorsCallbacks.add(callback)
    }

    fun register_collect_beacons_callback(callback: (BeaconLight) -> Unit) {
        beaconsCallbacks.add(callback)
    }

    fun register_direction_update_callback(callback: (Int, Int, Int, Float, Long) -> Unit) {
        directionCallbacks.add(callback)
    }

    fun register_double_tap_callback(callback: (Long) -> Unit) {
        doubleTapCallbacks.add(callback)
    }

    fun register_ping_callback(callback: (MutableList<FirmwareVersion?>) -> Unit) {
        pingCallbacks.add(callback)
    }

    fun unregister_collect_sensors_callback(callback: (AccelGyroInfo) -> Unit) {
        sensorsCallbacks.remove(callback)
    }

    fun unregister_collect_beacons_callback(callback: (BeaconLight) -> Unit) {
        beaconsCallbacks.remove(callback)
    }

    fun unregister_direction_update_callback(callback: (Int, Int, Int, Float, Long) -> Unit) {
        directionCallbacks.remove(callback)
    }

    fun unregister_ping_callback(callback: (MutableList<FirmwareVersion?>) -> Unit) {
        pingCallbacks.remove(callback)
    }

    fun set_rssi_offset(front: RssiOffsetValues, right: RssiOffsetValues, left: RssiOffsetValues) {
        baseRequest.set_rssi_offset(front, right, left)
    }

    companion object {
        fun bluetoothAutoInit(_context: Context,
                              connectionHandler: WagooConnectionHandler? = null,
                              mac: String? = null): WagooGlassesInterface {
            var wagooGlassesInterface = WagooGlassesInterface(null, connectionHandler, Unit)


            thread {
                WagooBluetoothManager.getBondedWagooDevices(object : WagooScanCallback() {
                    override fun onDeviceFound(device: WagooDevice) {
                        if (mac == null || device.mac == mac) {
                            wagooGlassesInterface.setDevice(device)
                            stopScan()
                        }
                    }
                    override fun onScanComplete(devices: List<WagooDevice>) {
                    }
                })
            }

            return wagooGlassesInterface
        }

        fun bleAutoInit(context: Context,
                        connectionHandler: WagooConnectionHandler? = null,
                        mac: String? = null): WagooGlassesInterface {
            val wagooGlassesInterface = WagooGlassesInterface(null, connectionHandler, Unit)

            GlobalScope.launch {
                val channel = WagooBleManager.scanBleWagooDevices(context)
                while (true) {
                    val device = channel.receive()
                    if (mac == null || device.mac == mac) {
                        wagooGlassesInterface.setDevice(device)
                        channel.close()
                        break
                    }
                }
            }

            return wagooGlassesInterface
        }
    }

    private val dataCollector = AutoDataCollector(this)

    fun getDataCollector(): AutoDataCollector {
        return dataCollector
    }

}