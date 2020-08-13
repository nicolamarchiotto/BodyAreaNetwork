package com.wagoo.wgcom

import java.nio.ByteBuffer
import java.util.*

internal object UuidUtils {
    fun asUuid(bytes: ByteArray): UUID {
        val bb: ByteBuffer = ByteBuffer.wrap(bytes)
        val firstLong: Long = bb.long
        val secondLong: Long = bb.long
        return UUID(firstLong, secondLong)
    }

    fun asBytes(uuid: UUID): ByteArray {
        val bb: ByteBuffer = ByteBuffer.wrap(ByteArray(16))
        bb.putLong(uuid.mostSignificantBits)
        bb.putLong(uuid.leastSignificantBits)
        return bb.array()
    }
}
