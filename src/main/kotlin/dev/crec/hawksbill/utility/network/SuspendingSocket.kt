package dev.crec.hawksbill.utility.network

import java.net.SocketAddress
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.CompletionHandler
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


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

    suspend fun read(buffer: ByteBuffer) {
        suspendCoroutine { continuation ->
            socketChannel.read(
                buffer, continuation, SocketCompletionHandler<Int>()
            )
        }
    }

    suspend fun write(buffer: ByteBuffer) {
        suspendCoroutine { continuation ->
            socketChannel.write(
                buffer, continuation, SocketCompletionHandler<Int>()
            )
        }
    }

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






