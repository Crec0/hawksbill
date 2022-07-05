package dev.crec.hawksbill.network

import dev.crec.hawksbill.bot
import dev.crec.hawksbill.log
import dev.crec.hawksbill.util.env
import java.io.BufferedInputStream
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets

class Connection {
    private val serverSocket: ServerSocket = ServerSocket(25576, 0, InetAddress.getByName("0.0.0.0"))

    fun startConnection() {
        while (true) {
            try {
                val socket = serverSocket.accept()
                socket.soTimeout = 500
                if (!socket.inetAddress.isSiteLocalAddress) {
                    log.info("is not loopback?")
                    continue
                }
                read(socket)
            } catch (ignored: Exception) {
            }
        }
    }

    private fun read(socket: Socket) {
        if (socket.isClosed) return
        val bufferStream = BufferedInputStream(socket.getInputStream())

        val bufferArray = ByteArray(MAX_READ_LENGTH)
        val bytesRead = bufferStream.read(bufferArray, 0, MAX_READ_LENGTH)
        bufferStream.close()

        if (bytesRead < 10) return

        val buffer = ByteBuffer.wrap(bufferArray).order(ByteOrder.LITTLE_ENDIAN)
        val size = buffer.int

        // If payload size is 0, we ignore
        val payloadSize = size - Integer.BYTES - Byte.SIZE_BYTES * 2
        if (payloadSize <= 0) return

        val type = when (buffer.int) {
            69 -> RconType.CHAT_BRIDGE
            else -> RconType.UNKNOWN
        }
        if (type == RconType.UNKNOWN) return

        val payload = ByteArray(payloadSize)
        buffer.get(payload, 0, payloadSize)
        buffer.clear()

        val message = payload.toString(StandardCharsets.UTF_8)

        val channel = bot.jda.getTextChannelById(env("CHAT_BRIDGE_CHANNEL"))
        channel?.sendMessage(message)?.queue()
    }
}
