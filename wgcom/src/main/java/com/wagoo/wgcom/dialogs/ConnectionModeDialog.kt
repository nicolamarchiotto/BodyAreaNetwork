package com.wagoo.wgcom.dialogs

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import kotlin.coroutines.Continuation
import kotlin.coroutines.suspendCoroutine

class ConnectionModeDialog private constructor(
        private val continuation: Continuation<Int>
) : DialogFragment() {


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog { // Use the Builder class for convenient dialog construction
        val builder = AlertDialog.Builder(getActivity())
        builder.setMessage("Select connection mode")
                .setPositiveButton("BLE") { dialog: DialogInterface?, id: Int ->
                    continuation.resumeWith(Result.success(BLE_CONNECTION))
                }
                .setNeutralButton("BLUETOOTH") { dialog: DialogInterface?, id: Int ->
                    continuation.resumeWith(Result.success(BLUETOOTH_CONNECTION))
                }
        // Create the AlertDialog object and return it
        return builder.create()
    }

    companion object {

        const val BLUETOOTH_CONNECTION = 1
        const val BLE_CONNECTION = 2

        suspend fun chooseConnectionMode(activity: FragmentActivity): Int {
            return suspendCoroutine<Int> {
                val dialog = ConnectionModeDialog(it)
                dialog.isCancelable = false
                dialog.show(activity.supportFragmentManager, "com.wagoo.exampleapp.BLECONNMODEDIALOG")
            }
        }
    }
}