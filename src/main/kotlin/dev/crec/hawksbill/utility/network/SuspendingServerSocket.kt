package dev.crec.hawksbill.utility.network

import kotlinx.coroutines.runBlocking
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousServerSocketChannel
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.CompletionHandler
import java.nio.charset.StandardCharsets
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class SuspendingServerSocket {
    private val socketChannel = AsynchronousServerSocketChannel.open()

    fun bind(address: SocketAddress) {
        socketChannel.bind(address)
    }

    fun close() {
        socketChannel.close()
    }

    suspend fun accept(): SuspendingSocket {
        return suspendCoroutine { continuation ->
            socketChannel.accept(continuation, SocketCompletionHandler())
        }
    }

    private class SocketCompletionHandler
        : CompletionHandler<AsynchronousSocketChannel, Continuation<SuspendingSocket>> {

        override fun completed(
            result: AsynchronousSocketChannel,
            attachment: Continuation<SuspendingSocket>
        ) {
            attachment.resume(SuspendingSocket(result))
        }

        override fun failed(
            exc: Throwable,
            attachment: Continuation<SuspendingSocket>
        ) {
            attachment.resumeWithException(exc)
        }
    }
}

fun main() {
    runBlocking {
        val serverSocket = SuspendingServerSocket()
        serverSocket.bind(InetSocketAddress(12345))
        println("Bound")
        while (true) {
            println("Awaiting connection")
            val socket = serverSocket.accept()
            println("Accepted socket")
            val buffer = ByteBuffer.allocate(1024)
            val bytesRead = socket.read(buffer)
            buffer.rewind()
            println("Socket read: $bytesRead")
            val payloadSize = buffer.int
            println("Payload size: $payloadSize")
            val payload = ByteArray(payloadSize)
            buffer.get(payload, 0, payloadSize)
            buffer.clear()
            val message = payload.toString(StandardCharsets.UTF_8)
            println(">>> $message")
        }
    }
}
