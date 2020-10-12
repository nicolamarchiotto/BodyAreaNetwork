@file:Suppress("JniMissingFunction")

/* This file is auto-generated from rpc-lib auto definitions");
 * DO NOT EDIT BY HAND or changes will be overwritten!!");
 */
package com.wagoo.wgcom.functions.core_functions

import com.wagoo.wgcom.functions.MainRustInterop


data class FileParts(val parts_num: Byte, val parts: IntArray)
enum class PingMode(val value: Int) {
    MasterVersion(0),
    RightVersion(1),
    LeftVersion(2),
    BridgeVersion(3),
    KeepAwake(4);
    companion object {
        @JvmStatic
        fun fromValue(value: Int): PingMode? {
            return when (value) {
                0 -> MasterVersion
                1 -> RightVersion
                2 -> LeftVersion
                3 -> BridgeVersion
                4 -> KeepAwake
                else -> null
            }
        }
    }
}


internal abstract class KotlinReceiverCoreFunctions : MainRustInterop.Receiver() {

    private external fun registerNative()
    override fun registerNativeRef() {
        registerNative()
    }

    abstract fun pong(mode: PingMode, status: Int, major: Int, minor: Int, patch: Int)
    abstract fun file_request(path: String, fid: Int)
    abstract fun file_request_parts(fid: Int, parts: FileParts)
    abstract fun update_notify_progress(chip: Byte, progress: Byte)
    abstract fun request_bootup(boot_id: Long)
}
internal abstract class KotlinSenderCoreFunctions : MainRustInterop.Sender() {

    private external fun registerNative()
    override fun registerNativeRef() {
        registerNative()
    }

    external fun ping(mode: PingMode)
    external fun update_request(master_path: String, slave_path: String)
    external fun file_send_info(fid: Int, checksum: Int, block_size: Int, file_size: Int)
    external fun file_send_part(fid: Int, checksum: Int, start: Int, xdata: ByteArray)
    external fun file_inform_all_packets_sent(fid: Int)
    external fun reboot(chip: Byte, delay: Int, magic: Int)
    external fun bootup_glasses(info: ByteArray)
    external fun request_mtu(value: Int)
    external fun disable_logging()
    external fun enable_logging()
    external fun start_chip_update(firmw_code: Int, path: String)
}
