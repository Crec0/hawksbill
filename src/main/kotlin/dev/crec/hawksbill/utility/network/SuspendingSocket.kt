package dev.crec.hawksbill.utility.network

import java.net.SocketAddress
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.CompletionHandler
import java.nio.channels.InterruptedByTimeoutException
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
    suspend fun connect(address: SocketAddress) {
        suspendCoroutine { continuation ->
            socketChannel.connect(
                address, continuation, SocketCompletionHandler<Void>()
            )
        }
    }

    suspend fun read(buffer: ByteBuffer, timeout: Duration) {
        suspendCoroutine { continuation ->
            socketChannel.read(
                buffer, timeout.inWholeMilliseconds, TimeUnit.MILLISECONDS, continuation, SocketCompletionHandler<Int>()
            )
        }
    }

    suspend fun read(buffer: ByteBuffer) = read(buffer, 50.milliseconds)

    suspend fun write(buffer: ByteBuffer, timeout: Duration) {
        suspendCoroutine { continuation ->
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

    private class SocketCompletionHandler<T>
        : CompletionHandler<T, Continuation<T>> {

        override fun completed(result: T, attachment: Continuation<T>) {
            attachment.resume(result)
        }

        override fun failed(exc: Throwable, attachment: Continuation<T>) {
            attachment.resumeWithException(exc)
        }
    }
}






