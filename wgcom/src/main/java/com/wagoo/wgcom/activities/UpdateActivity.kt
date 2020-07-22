package com.wagoo.wgcom.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.wagoo.utilities.PermissionAsyncRequester
import com.wagoo.wgcom.R
import com.wagoo.wgcom.WagooGlassesInterface
import com.wagoo.wgcom.connection.WagooConnectionHandler
import com.wagoo.wgcom.connection.WagooDevice
import com.wagoo.wgcom.dialogs.ConnectionModeDialog
import kotlinx.android.synthetic.main.activity_update.*
import kotlinx.android.synthetic.main.content_update.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.concurrent.thread

class UpdateActivity : AppCompatActivity() {


    var glassesInterface: WagooGlassesInterface? = null
    val permissionAsyncRequester = PermissionAsyncRequester(this)

    val corutineUpdaterContext = CoroutineScope(Dispatchers.Default)

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionAsyncRequester.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    fun startGlasses() {

        val handler = object : WagooConnectionHandler() {

                    val textVersionBoxes = arrayListOf(masterVersion,
                            rightSlaveVersion,
                            leftSlaveVersion)

                    override fun onDeviceFound(wagooInterface: WagooGlassesInterface, device: WagooDevice) {
                        wagooInterface.connect()
                    }

                    override fun onConnected(wagooInterface: WagooGlassesInterface) {
                        glassesInterface?.ping_glasses()
                        connectionUpdate(2)

                        glassesInterface?.register_ping_callback {
                            runOnUiThread {
                                for (i in 0 until 3) {
                                    textVersionBoxes[i].text = if (it[i] == null) {
                                        "loading..."
                                    } else {
                                        val version= it[i]!!
                                        "${version.major}.${version.minor}.${version.patch}"
                                    }
                                }
                            }
                        }
                        Thread.sleep(3000)
                        glassesInterface?.ping_glasses()
                    }

                    override fun onConnecting(wagooInterface: WagooGlassesInterface) {
                        connectionUpdate(1)
                    }

                    override fun onDisconnected(wagooInterface: WagooGlassesInterface) {
                        connectionUpdate(0)
                    }

                    fun connectionUpdate(status: Int) {
                        this@UpdateActivity.window?.decorView?.rootView?.let { view ->
                            Snackbar.make(view, "Connection status: " +
                                    when (status) {
                                        0 -> "disconnected"
                                        1 -> "connecting"
                                        2 -> "connected"
                                        else -> "unknown"
                                    }
                                    , Snackbar.LENGTH_SHORT)
//                        .setAction("Action", null)
                                    .show()
                        }
                    }
                }

        corutineUpdaterContext.launch {

            val connectionMode = ConnectionModeDialog.chooseConnectionMode(this@UpdateActivity)

            if (connectionMode == ConnectionModeDialog.BLE_CONNECTION)
            {
                permissionAsyncRequester.requestBlePermissions()
                glassesInterface = WagooGlassesInterface.bleAutoInit(this@UpdateActivity.applicationContext, handler)
            }
            else if (connectionMode == ConnectionModeDialog.BLUETOOTH_CONNECTION){
                glassesInterface = WagooGlassesInterface.bluetoothAutoInit(this@UpdateActivity.applicationContext, handler)
            }

        }

    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update)
        setSupportActionBar(toolbar)

        val url = intent?.getStringExtra(UPDATE_URL)

        startGlasses()

        updateButton.setOnClickListener {

            masterProgressBar.progress = 0
            masterProgressBar.visibility = View.VISIBLE
            rightSlaveProgressBar.progress = 0
            rightSlaveProgressBar.visibility = View.VISIBLE
            leftSlaveProgressBar.progress = 0
            leftSlaveProgressBar.visibility = View.VISIBLE
            checkMaster.uncheck()
            checkRightSlave.uncheck()
            checkLeftSlave.uncheck()
            var completed = 0

            var rebootRequested = false
//            WagooGlassesInterface.set_lights(0.7f, 500, false, true, false)

            url?.let {
                val glassesInterface = glassesInterface ?: return@let
                glassesInterface.firmware_update_online(this, url) { chip, progress, chipRebootRequested ->
                    if (chipRebootRequested) {
                        rebootRequested = true
                    }

                    val (progressBar, checkMark, versionText) = when (chip) {
                        0 -> Triple(masterProgressBar, checkMaster, masterVersion)
                        1 -> Triple(rightSlaveProgressBar, checkRightSlave, rightSlaveVersion)
                        2 -> Triple(leftSlaveProgressBar, checkLeftSlave, leftSlaveVersion)
                        else -> return@firmware_update_online
                    }

                    if (progress == 100 && progressBar.visibility == View.VISIBLE) {
                        versionText.text = "$progress%"
                        progressBar.visibility = View.INVISIBLE
                        runOnUiThread { checkMark.check() }
                        completed += 1
                        if (completed == 3) {
                            if (rebootRequested) {
                                glassesInterface.stop_request_version()
                                masterVersion.text = getString(R.string.reboot_prompt)
                                rightSlaveVersion.text = getString(R.string.reboot_prompt)
                                leftSlaveVersion.text = getString(R.string.reboot_prompt)
                                glassesInterface.reboot(1, 2000)
                                glassesInterface.reboot(2, 2000)
                                glassesInterface.reboot(0, 2000)
                                thread {
                                    Thread.sleep(4000)
                                    glassesInterface.request_version()
                                }
                            } else {
                                glassesInterface.request_version()
//                            WagooGlassesInterface.set_lights(0.0f, 0, false, false, false)
                            }
                        }
                    } else {
                        progressBar.progress = progress
                        versionText.text = "$progress%"
                    }
                }
            }
            glassesInterface?.request_version()
        }

        masterProgressBar.visibility = View.INVISIBLE
        rightSlaveProgressBar.visibility = View.INVISIBLE
        leftSlaveProgressBar.visibility = View.INVISIBLE
    }

    companion object {
        val UPDATE_URL = "com.wagoo.updater.UPDATE_URL"
    }

}
