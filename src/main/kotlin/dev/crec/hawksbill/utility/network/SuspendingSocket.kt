package dev.crec.hawksbill.utility.network

import kotlinx.coroutines.runBlocking
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.CompletionHandler
import java.util.concurrent.TimeUnit
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds


class SuspendingSocket(
    private val socketChannel: AsynchronousSocketChannel = AsynchronousSocketChannel.open()
) {
    val localAddress: InetAddress
        get() = (socketChannel.localAddress as InetSocketAddress).address

    val remoteAddress: InetAddress
        get() = (socketChannel.remoteAddress as InetSocketAddress).address

    val isOpen: Boolean
        get() = socketChannel.isOpen

    val isClosed: Boolean
        get() = !isOpen

    suspend fun connect(address: SocketAddress) {
        suspendCoroutine { continuation ->
            socketChannel.connect(
                address, continuation, SocketCompletionHandler<Void>()
            )
        }
    }

    suspend fun read(buffer: ByteBuffer, timeout: Duration): Int {
        return suspendCoroutine { continuation ->
            socketChannel.read(
                buffer, timeout.inWholeMilliseconds, TimeUnit.MILLISECONDS, continuation, SocketCompletionHandler<Int>()
            )
        }
    }

    suspend fun read(buffer: ByteBuffer) = read(buffer, 50.milliseconds)

    suspend fun write(buffer: ByteBuffer, timeout: Duration): Int {
        return suspendCoroutine { continuation ->
            socketChannel.write(
                buffer, timeout.inWholeMilliseconds, TimeUnit.MILLISECONDS, continuation, SocketCompletionHandler<Int>()
            )
        }
    }

    suspend fun write(buffer: ByteBuffer) = write(buffer, 50.milliseconds)

    suspend fun close() {
        suspendCoroutine<Unit> {
            socketChannel.close()
        }
    }

    private class SocketCompletionHandler<T> : CompletionHandler<T, Continuation<T>> {

        override fun completed(result: T, attachment: Continuation<T>) {
            attachment.resume(result)
        }

        override fun failed(exc: Throwable, attachment: Continuation<T>) {
            attachment.resumeWithException(exc)
        }
    }
}

fun main() {
    runBlocking {
        val socket = SuspendingSocket()
        socket.connect(InetSocketAddress("127.0.0.1", 12345))
        println("socket connected")
        val buffer = ByteBuffer.allocate(100)
        val data = "hello".toByteArray()
        buffer.putInt(data.size)
        buffer.put(data)
        buffer.rewind()
        socket.write(buffer)
        println("socket written")
        buffer.clear()
    }
}




