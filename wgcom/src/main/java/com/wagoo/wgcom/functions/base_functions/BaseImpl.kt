package com.wagoo.wgcom.functions.base_functions

import com.wagoo.wgcom.WagooGlassesInterface

internal class BaseResponseImpl(private val wagooInterface: WagooGlassesInterface) : KotlinReceiverBaseFunctions() {

    override fun collect_sensors_send(data: AccelGyroInfo) {
        wagooInterface.on_sensors_data(data)
    }

    override fun collect_beacon_send(data: BeaconLight) {
        wagooInterface.on_beacons_data(data)
    }

    override fun beacon_send_direction(wid: Byte, dir: Byte, accuracy: Byte, findex: Float, elapsed_us_since_last: Long) {
        wagooInterface.on_direction_data(wid.toInt(), dir.toInt(), accuracy.toInt(), findex, elapsed_us_since_last / 1000)
    }

    override fun notify_double_tap(timestamp_us: Long) {
        wagooInterface.on_double_tap(timestamp_us)
    }
}

internal class BaseRequestImpl(private val wagooInterface: WagooGlassesInterface) : KotlinSenderBaseFunctions()