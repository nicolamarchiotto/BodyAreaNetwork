package com.wagoo.wgcom

internal class WagooHandlerClass<T> {
    private val handlers = mutableListOf<T>()

    fun add(handler: T) {
        if (!handlers.contains(handler))
            handlers.add(handler)
    }

    fun remove(handler: T) {
        handlers.remove(handler)
    }

    fun clear() {
        handlers.clear()
    }

    inline fun execute(funct: (T) -> Unit) {
        for (handler in handlers) funct(handler)
    }

}