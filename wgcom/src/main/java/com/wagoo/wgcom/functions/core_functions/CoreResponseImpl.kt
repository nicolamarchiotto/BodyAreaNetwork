package com.wagoo.wgcom.functions.core_functions

import android.util.Log
import com.wagoo.wgcom.FirmwareVersion
import com.wagoo.wgcom.WagooGlassesInterface


internal class CoreResponseImpl(private val wagooInterface: WagooGlassesInterface)
    : KotlinReceiverCoreFunctions() {

    override fun pong(mode: PingMode, status: Int, major: Int, minor: Int, patch: Int) {
        if (mode.value < wagooInterface.firmwareVersion.size) {
            wagooInterface.firmwareVersion[mode.value] = FirmwareVersion(major, minor, patch)
        }
        wagooInterface.on_ping_update()
        Log.d("PONG", "Version of chip ${mode.value}: $major.$minor.$patch")
    }

    override fun file_request(path: String, fid: Int) {
        wagooInterface.updater.send_firmware(fid, path)
    }

    override fun file_request_parts(fid: Int, parts: FileParts) {
        wagooInterface.updater.send_missing_blocks(fid, parts.parts.sliceArray(0 until parts.parts_num))
    }

    override fun update_notify_progress(chip: Byte, progress: Byte) {
        wagooInterface.updater.notify_progress(chip, progress)
    }

    override fun request_bootup(boot_id: Long) {
    }
}

internal class CoreRequestImpl(private val wagooInterface: WagooGlassesInterface) : KotlinSenderCoreFunctions()