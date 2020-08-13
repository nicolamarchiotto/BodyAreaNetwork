package com.wagoo.wgcom

import android.content.Context
import android.util.Log
import com.wagoo.wgcom.functions.core_functions.CoreRequestImpl
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.map
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.launch
import java.util.*
import java.util.zip.CRC32
import kotlin.collections.HashMap
import kotlin.concurrent.thread
import kotlin.math.min

internal class Updater(
        private val coreRequest: CoreRequestImpl,
        var blockSize: Int = 192) {



    var files_id: HashMap<Int, ByteArray> = HashMap()
    var files: HashMap<String, ByteArray> = HashMap()
    var progressCallback: ((Int, Int) -> Unit)? = null
    var transmitting = false

    var sendingQueue = mutableSetOf<Int>()
    var sendingQueueValidity: Long = 0
    var sendingIndex = 0

    var files_list: MutableList<Pair<Int, String>> = mutableListOf()

    fun start_firmware_update(master: ByteArray?, slave: ByteArray?, progress: (Int, Int) -> Unit) {
        progressCallback = progress
        coreRequest.update_request(
            master?.let {
                files["master"] = it
                "master"
            } ?: "",
            slave?.let {
                files["slave"] = it
                "slave"
            } ?: "")

    }

    fun start_firmware_update(context: Context, master: Boolean, slave: Boolean, progress: (Int, Int) -> Unit) {
        var masterFirmware: ByteArray? = null
        var slaveFirmware: ByteArray? = null
        if (master) {
            masterFirmware = context.resources.openRawResource(R.raw.signed_master).readBytes()
        }
        if (slave) {
            slaveFirmware = context.resources.openRawResource(R.raw.signed_slave).readBytes()
        }
        start_firmware_update(masterFirmware, slaveFirmware, progress)
    }

    var receptionCoroutine: Job? = null

    fun ensure_final_reception(fid: Int, localIndex: Int) {
        receptionCoroutine?.cancel()
        receptionCoroutine = GlobalScope.launch {
            val ticker = ticker(2000, 500)

            while (true) {
                ticker.receive()
                if (sendingIndex == localIndex) {
                    coreRequest.file_inform_all_packets_sent(fid)
                    Log.d("UPDATER", "Sent final reception message!")
                } else {
                    ticker.cancel()
                    receptionCoroutine = null
                    break
                }
            }
        }
    }

    fun send_firmware(fid: Int, path: String) {

        val file = files[path] ?: return
        files_id[fid] = file

        var globalCrc = CRC32()
        globalCrc.update(file)

        coreRequest.file_send_info(fid, globalCrc.value.toInt(), blockSize, file.size)

        if (!transmitting) {
            transmitting = true
            thread {
                var i = 0
                while (i < file.size) {
                    var crc = CRC32()
                    val slice = file.sliceArray(i until min(i + blockSize, file.size))

                    crc.update(slice)
                    // FIXME: Check why randomizing packets leads to a corrupted firmware!
                    coreRequest.file_send_part(fid, crc.value.toInt(), i, slice)
                    Log.d("UPDATER", "Sent block ${i / blockSize}")
                    i += blockSize
                }
                coreRequest.file_inform_all_packets_sent(fid)
                sendingIndex += 1
                ensure_final_reception(fid, sendingIndex)
            }
        }
        else {
            if (!files_list.contains(fid to path)) {
                files_list.add(fid to path)
            }
        }
    }

    fun send_missing_blocks(fid: Int, blocks: IntArray) {

        val file = files_id[fid] ?: return

        if (sendingQueueValidity < System.currentTimeMillis() - 1000) {
            sendingQueue.clear()
            sendingQueueValidity = System.currentTimeMillis()
        }

        for (block in blocks) {
            if (sendingQueue.contains(block)) continue
            sendingQueue.add(block)
            val address = block * blockSize
            var crc = CRC32()
            val slice = file.sliceArray(address until min(address+blockSize, file.size))

            crc.update(slice)
            coreRequest.file_send_part(fid, crc.value.toInt(), address, slice)
            Log.d("UPDATER", "Resent block $block")
        }
        coreRequest.file_inform_all_packets_sent(fid)
        sendingIndex += 1
        ensure_final_reception(fid, sendingIndex)
    }

    fun notify_progress(chip: Byte, progress: Byte) {

        if (progress == 100.toByte()) {
            sendingIndex += 1
            transmitting = false
            if (files_list.isNotEmpty()) {
                val (fid, file) = files_list[0]
                files_list.removeAt(0)
                send_firmware(fid, file)
            }
        }

        progressCallback?.invoke(chip.toInt(), progress.toInt())
    }
}