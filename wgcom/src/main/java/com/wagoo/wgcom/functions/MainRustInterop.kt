@file:Suppress("JniMissingFunction")
package com.wagoo.wgcom.functions

class MainRustInterop(private val sendCallback: ((ByteArray) -> Unit)) {

    private external fun nativeInit()

    private val receivers = mutableListOf<Receiver>()
    private val senders = mutableListOf<Sender>()

    abstract class NativeReference protected constructor() {
        private var nativeRef: Long = -1
        abstract fun registerNativeRef()
        internal fun setNative(ref: Long) {
            nativeRef = ref
            registerNativeRef()
        }
    }

    abstract class Receiver : NativeReference()
    abstract class Sender : NativeReference()

    private val nativeRef = ++internal

    init {
        nativeInit()
    }

    private fun sendData(packet: ByteArray) {
        sendCallback(packet)
    }

    fun addReceiver(receiver: Receiver) {
        receiver.setNative(nativeRef)
        receivers.add(receiver)
    }

    fun registerSender(sender: Sender) {
        sender.setNative(nativeRef)
        senders.add(sender)
    }

    // Direct link to native function!
    external fun setMtu(mtu: Int)
    external fun processData(data: ByteArray)

    companion object {
        private var internal: Long
        init {
            System.loadLibrary("android_native_protocol")
            internal = 0
        }
    }
}