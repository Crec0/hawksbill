package dev.crec.hawksbill.modules.rcon

import java.nio.ByteBuffer
import java.nio.ByteOrder

class RconPacket(
    val requestId: Int = 0,
    val type: RconResponse = RconResponse.UNKNOWN,
    val payload: ByteArray = ByteArray(0)
) {
    companion object {
        fun fromBuffer(expectedId: Int, buffer: ByteBuffer): Result<RconPacket> {
            val length = buffer.int
            val requestId = buffer.int

            if (requestId != expectedId) {
                return Result.failure(IllegalStateException("Unexpected response ($expectedId -> $requestId)"))
            }

            val type = when (buffer.int) {
                2 -> RconResponse.LOGIN_SUCCESS
                else -> RconResponse.UNKNOWN
            }

            val nullByteOffset = length - Byte.SIZE_BYTES * 2
            val payloadLength = nullByteOffset - Integer.BYTES * 2

            val payload = ByteArray(payloadLength)

            if (payloadLength > 0) {
                buffer.get(payload, 0, payloadLength)
            }

            return Result.success(
                RconPacket(requestId = requestId, type = type, payload = payload)
            )
        }
    }

    /**
     * RconPacket [
     *      length         : Int
     *      requestId      : Int
     *      type           : Int
     *      payload        : byte[]
     *      padding (null) : byte
     *      padding (null) : byte
     * ]
     */
    fun intoBuffer(): ByteBuffer {
        val length = Integer.BYTES * 3 + payload.size + Byte.SIZE_BYTES * 2
        return ByteBuffer.allocate(length).order(ByteOrder.LITTLE_ENDIAN).apply {
            putInt(length)
            putInt(requestId)
            putInt(type.value)
            put(payload)
            put(0)
            put(0)
            rewind()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RconPacket) return false

        if (requestId != other.requestId) return false
        if (type != other.type) return false
        if (!payload.contentEquals(other.payload)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = requestId
        result = 31 * result + type.hashCode()
        result = 31 * result + payload.contentHashCode()
        return result
    }
}
