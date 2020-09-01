package com.wagoo.wgcom.activities

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.wagoo.utilities.PermissionAsyncRequester
import com.wagoo.wgcom.R
import com.wagoo.wgcom.WagooGlassesInterface
import com.wagoo.wgcom.connection.WagooBleCallbackDebug
import com.wagoo.wgcom.connection.WagooBleManager
import com.wagoo.wgcom.connection.WagooConnectionHandler
import com.wagoo.wgcom.connection.WagooDevice
import com.wagoo.wgcom.dialogs.ConnectionModeDialog
import com.wagoo.wgcom.fragments.CheckedLineFragment
import com.wagoo.wgcom.fragments.CheckedStatus
import kotlinx.android.synthetic.main.activity_connlog.*
import kotlinx.android.synthetic.main.content_update.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ConnectionLogActivity : AppCompatActivity() {


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
                        this@ConnectionLogActivity.window?.decorView?.rootView?.let { view ->
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

            val connectionMode = ConnectionModeDialog.chooseConnectionMode(this@ConnectionLogActivity)

            if (connectionMode == ConnectionModeDialog.BLE_CONNECTION)
            {
                permissionAsyncRequester.requestBlePermissions()
                glassesInterface = WagooGlassesInterface.bleAutoInit(this@ConnectionLogActivity.applicationContext, handler)
            }
            else if (connectionMode == ConnectionModeDialog.BLUETOOTH_CONNECTION){
                glassesInterface = WagooGlassesInterface.bluetoothAutoInit(this@ConnectionLogActivity.applicationContext, handler)
            }

        }

    }

    fun addLine(description: String): CheckedLineFragment {
        val fragment = CheckedLineFragment(description)
        supportFragmentManager.beginTransaction().add(listLayout.id, fragment).commit()
        return fragment
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_connlog)

        val scan = addLine("Scan")
        val connect = addLine("Connection")
        val mtu = addLine("Mtu set")
        val discovery = addLine("Service discovery")
        val nordicService = addLine("Nordic service")
        val nordicReadChar = addLine("Nordic read characteristic")
        val nordicWriteChar = addLine("Nordic write characteristic")
        val nordicPingMaster = addLine("Nordic ping master")
        val nordicPingRight = addLine("Nordic ping right")
        val nordicPingLeft = addLine("Nordic ping left")

        val wagooGlassesInterface = WagooGlassesInterface(null, object: WagooConnectionHandler() {
            override fun onDeviceFound(wagooInterface: WagooGlassesInterface, device: WagooDevice) {}

            override fun onConnected(wagooInterface: WagooGlassesInterface) {
            }

            override fun onConnecting(wagooInterface: WagooGlassesInterface) {
            }

            override fun onDisconnected(wagooInterface: WagooGlassesInterface) {
            }

        }, Unit)

        wagooGlassesInterface.register_ping_callback {
            if (it[0] != null && nordicPingMaster.checkedStatus != CheckedStatus.Done) {
                nordicPingMaster.checkedStatus = CheckedStatus.Done
                try { Thread.sleep(200) } finally { }
                nordicPingMaster.value = "${it[0]!!.major}.${it[0]!!.minor}.${it[0]!!.patch}"
            }
            if (it[1] != null && nordicPingRight.checkedStatus != CheckedStatus.Done) {
                nordicPingRight.checkedStatus = CheckedStatus.Done
                try { Thread.sleep(200) } finally { }
                nordicPingRight.value = "${it[1]!!.major}.${it[1]!!.minor}.${it[1]!!.patch}"
            }
            if (it[2] != null && nordicPingLeft.checkedStatus != CheckedStatus.Done) {
                nordicPingLeft.checkedStatus = CheckedStatus.Done
                try { Thread.sleep(200) } finally { }
                nordicPingLeft.value = "${it[2]!!.major}.${it[2]!!.minor}.${it[2]!!.patch}"
            }
        }

        GlobalScope.launch {
            permissionAsyncRequester.requestBlePermissions()
            val channel = WagooBleManager.scanBleWagooDevices(this@ConnectionLogActivity)
            scan.checkedStatus = CheckedStatus.InProgress
            val device = channel.receive()
            wagooGlassesInterface.setDevice(device)
            channel.close()
            scan.checkedStatus = CheckedStatus.Done
            connect.checkedStatus = CheckedStatus.InProgress

            val bleManager = device.connectionManager as WagooBleManager

            bleManager.debug = object : WagooBleCallbackDebug {
                override fun onConnection() {
                    connect.checkedStatus = CheckedStatus.Done
                    mtu.checkedStatus = CheckedStatus.InProgress
                }

                override fun onMtuSet() {
                    mtu.checkedStatus = CheckedStatus.Done
                    discovery.checkedStatus = CheckedStatus.InProgress
                }

                override fun onServiceDiscovered() {
                    discovery.checkedStatus = CheckedStatus.Done
                    nordicService.checkedStatus = CheckedStatus.InProgress
                }

                override fun onNordicServiceFound() {
                    nordicService.checkedStatus = CheckedStatus.Done
                    nordicReadChar.checkedStatus = CheckedStatus.InProgress
                    nordicWriteChar.checkedStatus = CheckedStatus.InProgress
                }

                override fun onReadCharacteristicFound() {
                    nordicReadChar.checkedStatus = CheckedStatus.Done
                }

                override fun onWriteCharacteristicFound() {
                    nordicWriteChar.checkedStatus = CheckedStatus.Done
                }

                override fun onConnectionComplete() {
                    nordicPingMaster.checkedStatus = CheckedStatus.InProgress
                    nordicPingRight.checkedStatus = CheckedStatus.InProgress
                    nordicPingLeft.checkedStatus = CheckedStatus.InProgress
                    wagooGlassesInterface.ping_glasses()
                }

                override fun onDisconnection() {
                }
            }

            wagooGlassesInterface.connect()
        }

//        getFragmentManager().beginTransaction().add(listLayout.id, fragment, "someTag2").commit();
    }
}
