@file:Suppress("JniMissingFunction")

/* This file is auto-generated from rpc-lib auto definitions");
 * DO NOT EDIT BY HAND or changes will be overwritten!!");
 */
package com.wagoo.wgcom.functions.base_functions

import com.wagoo.wgcom.functions.MainRustInterop


enum class ChipIdentity(val value: Int) {
    Master(0),
    RightSlave(1),
    LeftSlave(2),
    Bridge(3);
    companion object {
        @JvmStatic
        fun fromValue(value: Int): ChipIdentity? {
            return when (value) {
                0 -> Master
                1 -> RightSlave
                2 -> LeftSlave
                3 -> Bridge
                else -> null
            }
        }
    }
}

data class BeaconLight(val antenna: ChipIdentity, val wid: Byte, val timestamp: Long, val channel: Byte, val rssi: Byte, val crcok: Byte, val index: Int)
data class AccelGyroInfo(val timestamp: Long, val gyro: AngularRate, val accl: LinearAcceleration)
data class LightMode(val intensity: Int, val frequency_in_ms: Int)
data class LightsConfig(val mode: LightMode, val red: Boolean, val yellow: Boolean, val green: Boolean)
data class RssiOffsetValues(val ch37: Int, val ch38: Int, val ch39: Int)
enum class AccelerometerOdr(val value: Int) {
    PowerOff(0),
    Freq12h5Hz(1),
    Freq26Hz(2),
    Freq52Hz(3),
    Freq104Hz(4),
    Freq208Hz(5),
    Freq416Hz(6),
    Freq833Hz(7),
    Freq1660Hz(8),
    Freq3300Hz(9),
    Freq6600Hz(10);
    companion object {
        @JvmStatic
        fun fromValue(value: Int): AccelerometerOdr? {
            return when (value) {
                0 -> PowerOff
                1 -> Freq12h5Hz
                2 -> Freq26Hz
                3 -> Freq52Hz
                4 -> Freq104Hz
                5 -> Freq208Hz
                6 -> Freq416Hz
                7 -> Freq833Hz
                8 -> Freq1660Hz
                9 -> Freq3300Hz
                10 -> Freq6600Hz
                else -> null
            }
        }
    }
}

enum class GyroscopeOdr(val value: Int) {
    PowerOff(0),
    Freq12h5Hz(1),
    Freq26Hz(2),
    Freq52Hz(3),
    Freq104Hz(4),
    Freq208Hz(5),
    Freq416Hz(6),
    Freq833Hz(7),
    Freq1660Hz(8);
    companion object {
        @JvmStatic
        fun fromValue(value: Int): GyroscopeOdr? {
            return when (value) {
                0 -> PowerOff
                1 -> Freq12h5Hz
                2 -> Freq26Hz
                3 -> Freq52Hz
                4 -> Freq104Hz
                5 -> Freq208Hz
                6 -> Freq416Hz
                7 -> Freq833Hz
                8 -> Freq1660Hz
                else -> null
            }
        }
    }
}

data class LinearAcceleration(val x: Float, val y: Float, val z: Float)
data class AngularRate(val yaw: Float, val pitch: Float, val roll: Float)

internal abstract class KotlinReceiverBaseFunctions : MainRustInterop.Receiver() {

    private external fun registerNative()
    override fun registerNativeRef() {
        registerNative()
    }

    abstract fun collect_sensors_send(data: AccelGyroInfo)
    abstract fun collect_beacon_send(data: BeaconLight)
    abstract fun beacon_send_direction(wid: Byte, dir: Byte, accuracy: Byte, findex: Float, elapsed_us_since_last: Long)
}
internal abstract class KotlinSenderBaseFunctions : MainRustInterop.Sender() {

    private external fun registerNative()
    override fun registerNativeRef() {
        registerNative()
    }

    external fun disable_collect_mode()
    external fun enable_collect_mode()
    external fun whitelist_add_uuid(wid: Byte, uuid: ByteArray)
    external fun whitelist_add_mac(wid: Byte, mac: ByteArray)
    external fun whitelist_remove(wid: Byte)
    external fun whitelist_clear_all()
    external fun set_sensors_state(left_accel: AccelerometerOdr, right_accel: AccelerometerOdr, left_gyro: GyroscopeOdr, right_gyro: GyroscopeOdr)
    external fun set_lights(right_config: LightsConfig, left_config: LightsConfig)
    external fun set_rssi_offset(front: RssiOffsetValues, right: RssiOffsetValues, left: RssiOffsetValues)
}
