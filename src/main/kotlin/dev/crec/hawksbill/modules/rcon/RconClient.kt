package dev.crec.hawksbill.modules.rcon

import dev.crec.hawksbill.config.MinecraftServer
import dev.crec.hawksbill.utility.network.SuspendingSocket
import org.slf4j.LoggerFactory
import java.io.EOFException
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.atomic.AtomicInteger

class RconClient(val serverConfig: MinecraftServer) {
    private val log = LoggerFactory.getLogger("RCON-Client|${serverConfig.name}")
    private val socket = SuspendingSocket()
    private val requestCounter = AtomicInteger(0)

    companion object {
        const val MAX_PAYLOAD_SIZE = 4096
        const val PACKET_SIZE_WITHOUT_PAYLOAD = Integer.BYTES * 3 + Byte.SIZE_BYTES * 2

        const val MAX_READ_LENGTH = MAX_PAYLOAD_SIZE + PACKET_SIZE_WITHOUT_PAYLOAD
        const val MAX_WRITE_LENGTH = 1460
    }

    private suspend fun connect() {
        socket.connect(InetSocketAddress(serverConfig.serverIp, serverConfig.rconPort))
    }

    private suspend fun login() {
        val response = send(
            RconPacket(
                requestCounter.incrementAndGet(),
                RconResponse.LOGIN,
                serverConfig.rconPassword.toByteArray()
            )
        )
        if (response.isFailure || response.getOrThrow().type != RconResponse.LOGIN_SUCCESS) {
            throw IOException("Login failed")
        }
    }

    private suspend fun write(packet: RconPacket) = socket.write(packet.intoBuffer())

    private suspend fun read(packetId: Int): Result<RconPacket> {
        val buffer = ByteBuffer.allocate(MAX_READ_LENGTH).order(ByteOrder.LITTLE_ENDIAN)
        while (buffer.position() < Integer.BYTES) {
            if (socket.read(buffer) == -1) {
                throw EOFException("Channel reached end-of-stream")
            }
        }
        buffer.rewind()
        return RconPacket.fromBuffer(packetId, buffer)
    }

    suspend fun send(packet: RconPacket): Result<RconPacket> {
        val responseResult = runCatching {
            connect()
            write(packet)
            read(packet.requestId).getOrThrow()
        }

        if (responseResult.isFailure) {
            log.warn("Disconnected: ${responseResult.exceptionOrNull()?.message}")
        }

        return responseResult
    }
}
