package com.wagoo.wgcom

import android.os.Build
import com.google.gson.Gson
import com.google.gson.stream.JsonWriter
import com.wagoo.wgcom.functions.base_functions.AccelGyroInfo
import com.wagoo.wgcom.functions.base_functions.BeaconLight
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.ThreadLocalRandom

class AutoDataCollector internal constructor(
        private val wagooGlassesInterface: WagooGlassesInterface) {

    private var isStarted = false
    private var isConnected = false
    private var tokenPrefix = "unknown";
    private var randomToken = "token";
    private var fileWriter: FileWriter? = null
    private var jsonWriter: JsonWriter? = null
    private val gson = Gson()

    private val beaconsCollected = mutableListOf<BeaconLight>()
    private val sensorsCollected = mutableListOf<AccelGyroInfo>()
    private var handler: ((Long, String) -> Unit)? = null

    fun setTokenPrefix(token: String) {
        tokenPrefix = token
    }

    fun generateRandomToken(): String {
        val random: Random = ThreadLocalRandom.current()
        val r = ByteArray(4)
        random.nextBytes(r)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Base64.getEncoder().encodeToString(r).replace('/', '_')
        } else {
            "token"
        }
    }

    fun clear() {
        beaconsCollected.clear()
        sensorsCollected.clear()
    }

    fun start() {
        isStarted = true
        updateStatus()
    }

    fun stop() {
        isStarted = false
        updateStatus()
    }

    fun openFile(): FileWriter {
        randomToken = generateRandomToken()
        val date = LocalDateTime.now().toString()
        val filename = "$tokenPrefix-$randomToken-$date.json"
        val path = "/storage/emulated/0/wagoo/$filename"
        return FileWriter(File(path))
    }

    fun registerOnTimeUpdated(handler: (Long, String) -> Unit) {
        this.handler = handler
    }

    private val collectBeaconsCallback = {
        beacon: BeaconLight ->
        if (isStarted) {
            beaconsCollected.add(beacon)
            jsonWriter?.let {
                gson.toJson(gson.toJsonTree(beacon), it)
            }
            this.handler?.invoke(beacon.timestamp, randomToken)
        }
    }

    private val collectSensorsCallback = {
        sensors: AccelGyroInfo ->
        if (isStarted) {
            sensorsCollected.add(sensors)
            jsonWriter?.let {
                gson.toJson(gson.toJsonTree(sensors), it)
            }
            this.handler?.invoke(sensors.timestamp, randomToken)
        }
    }

    private fun updateStatus() {
        if (isConnected) {
            if (isStarted) {
                wagooGlassesInterface.enable_sensors()
                wagooGlassesInterface.enable_collect_mode()
            } else {
                wagooGlassesInterface.disable_sensors()
                wagooGlassesInterface.disable_collect_mode()
            }
        }
        if (isStarted) {
            fileWriter = openFile()
            jsonWriter = JsonWriter(BufferedWriter(fileWriter!!))
        }
        else {
            jsonWriter?.flush()
            jsonWriter?.close()
            fileWriter?.flush()
            fileWriter?.close()
            jsonWriter = null
            fileWriter = null
        }
    }

    internal fun onConnected() {
        randomToken = generateRandomToken()
        wagooGlassesInterface.register_collect_beacons_callback(collectBeaconsCallback)
        wagooGlassesInterface.register_collect_sensors_callback(collectSensorsCallback)
        isConnected = true
        updateStatus()
    }

    internal fun onDisconnected() {
        wagooGlassesInterface.unregister_collect_beacons_callback(collectBeaconsCallback)
        wagooGlassesInterface.unregister_collect_sensors_callback(collectSensorsCallback)
        isConnected = false
        updateStatus()
    }
}