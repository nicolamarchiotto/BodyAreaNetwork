package com.wagoo.utilities
import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.atomic.AtomicInteger

class PermissionAsyncRequester(val activity: Activity) {
    private val permissionRequestCounter = AtomicInteger(0)
    private val uid: Int
        get() = permissionRequestCounter.getAndIncrement()
    private val permissionListeners: MutableMap<Int, CancellableContinuation<Boolean>> = mutableMapOf()

    private fun requestPermissions(vararg permissions: String, continuation: CancellableContinuation<Boolean>) {
        val isRequestRequired =
                permissions
                        .map { ContextCompat.checkSelfPermission(activity, it) }
                        .any { result -> result == PackageManager.PERMISSION_DENIED }

        if(isRequestRequired) {
            val uid = uid
            permissionListeners[uid] = continuation
            ActivityCompat.requestPermissions(activity, permissions, uid)
        } else {
            continuation.resume(true) {}
        }
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        val isGranted = grantResults.all { result -> result == PackageManager.PERMISSION_GRANTED }
        permissionListeners
                .remove(requestCode)
                ?.resume(isGranted) {}
    }

    suspend fun requestPermissions(vararg permissions: String): Boolean =
            suspendCancellableCoroutine { continuation -> requestPermissions(*permissions, continuation = continuation) }

    suspend fun requestBlePermissions(): Boolean {
        // BLE
        // Richiesta dei permessi per la connessione ble
        // una volta che l'utente accetta i permessi,
        // viene chiamata in automatico la funzione onRequestPermissionsResult
        return this.requestPermissions(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
        )
    }
}