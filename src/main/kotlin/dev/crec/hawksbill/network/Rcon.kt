package dev.crec.hawksbill.network

import dev.crec.hawksbill.log
import dev.crec.hawksbill.util.env
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.ByteString.Companion.toByteString
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.SocketChannel
import java.util.concurrent.atomic.AtomicInteger

const val MAX_READ_LENGTH = 4110 // 4 + 4 + 4 + 4096 + 2
const val MAX_WRITE_LENGTH = 1460
const val MAX_PAYLOAD_LENGTH = MAX_WRITE_LENGTH - Integer.BYTES * 3 - Byte.SIZE_BYTES * 2

sealed interface Rcon {
    suspend fun login()
    suspend fun chatMessage(message: String)
    suspend fun send(packet: RconPacket): RconPacket
}

private var rconInstance = RconImpl() // This instance is refreshed on disconnect
fun rconInstance() = rconInstance

class RconImpl : Rcon {
    private val socket = SocketChannel.open(InetSocketAddress(env("RCON_IP"), env("RCON_PORT").toInt()))
    private val requestCounter = AtomicInteger(0)
    private val readingBuffer = ByteBuffer.allocate(MAX_READ_LENGTH).order(ByteOrder.LITTLE_ENDIAN)
    private val escapedPattern = Regex("\\\\([_*].*)") to "$1"
    private val gray = "§7"
    private val formattingMap = listOf(
        Regex("(?<![\\\\\\[])\\*\\*(.*)\\*\\*") to "§l$1$gray", // bold
        Regex("(?<![\\\\\\[])__(.*)__") to "§n$1$gray", // underline
        Regex("(?<![\\\\\\[])\\*(.*)\\*") to "§o$1$gray", // italic
        Regex("(?<![\\\\\\[])_([^_ ]+)_") to "§o$1$gray", // italic
        Regex("(?<![\\\\\\[])~~(.*)~~") to "§m$1$gray" // strike through
    )

    override suspend fun login() {
        val password = env("RCON_PASSWORD")
        val response = send(RconPacket(requestCounter.incrementAndGet(), RconType.LOGIN, password.toByteArray()))
        if (response.type != RconType.LOGIN_SUCCESS) {
            throw IOException("Login failed")
        }
    }

    override suspend fun chatMessage(message: String) {
        sanitize(message).chunked(MAX_PAYLOAD_LENGTH).forEach { chunk ->
            send(
                RconPacket(
                    requestCounter.incrementAndGet(),
                    RconType.CHAT_BRIDGE,
                    chunk.toByteArray()
                )
            )
        }
    }

    private fun sanitize(message: String): String {
        var sanitizedMessage = message
        formattingMap.forEach { pair ->
            sanitizedMessage = pair.first.replace(sanitizedMessage, pair.second)
        }
        while (escapedPattern.first.containsMatchIn(sanitizedMessage)) {
            sanitizedMessage = escapedPattern.first.replace(sanitizedMessage, escapedPattern.second)
        }
        return sanitizedMessage
    }

    override suspend fun send(packet: RconPacket): RconPacket {
        val id = packet.requestId
        return withContext(Dispatchers.IO) {
            try {
                socket.write(packet.write())
                awaitRead(id)
            } catch (e: IOException) {
                rconInstance = RconImpl() // Refresh the instance.
                log.warn("Disconnected: ${e.message}")
                RconPacket()
            }
        }
    }

    private suspend fun awaitRead(packetId: Int): RconPacket {
        return withContext(Dispatchers.IO) {
            readingBuffer.rewind()
            readingBuffer.clear()
            while (readingBuffer.position() < Integer.BYTES) {
                if (socket.read(readingBuffer) == -1) {
                    log.warn("EOF")
                    return@withContext RconPacket()
                }
            }
            readingBuffer.rewind()
            return@withContext RconPacket().read(packetId, readingBuffer)
        }
    }
}

class RconPacket(
    val requestId: Int = 0,
    val type: RconType = RconType.UNKNOWN,
    private val payload: ByteArray = ByteArray(0)
) {
    fun read(expectedId: Int, data: ByteBuffer): RconPacket {
        val length = data.int
        val requestId = data.int

        if (requestId != expectedId) {
            log.warn("Unexpected response ($expectedId -> $requestId)")
            return RconPacket()
        }

        val type = when (data.int) {
            2 -> RconType.LOGIN_SUCCESS
            69 -> RconType.CHAT_BRIDGE
            else -> RconType.UNKNOWN
        }

//        log.info("length: $length, request id: $requestId, type: $type")

        val nullByteOffset = length - Byte.SIZE_BYTES * 2
        val payloadLength = nullByteOffset - Integer.BYTES * 2

        val payload = ByteArray(payloadLength)

        if (payloadLength > 0) {
            data.get(payload, 0, payloadLength)
        }

        return RconPacket(
            requestId,
            type,
            payload
        )
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
    fun write(): ByteBuffer {
        val length = Integer.BYTES * 2 + payload.size + Byte.SIZE_BYTES * 2
        val buffer = ByteBuffer
            .allocate(Integer.BYTES + length)
            .order(ByteOrder.LITTLE_ENDIAN).apply {
                putInt(length)
                putInt(requestId)
                putInt(type.value)
                put(payload)
                put(0)
                put(0)
            }
        buffer.rewind()
//        log.info("$requestId, ${type.value}, ${payload.toByteString()}, ${buffer.position()}, ${buffer.remaining()}")
//        log.info("{}", buffer.array())
        return buffer
    }

    override fun toString(): String {
        return """
            Request id: $requestId
            Type:       $type
            Payload:    ${payload.toByteString()}
        """.trimIndent()
    }
}

enum class RconType(val value: Int) {
    UNKNOWN(-1),
    COMMAND_RESPONSE(0),
    COMMAND(2),
    LOGIN_SUCCESS(2),
    LOGIN(3),
    CHAT_BRIDGE(69)
}
