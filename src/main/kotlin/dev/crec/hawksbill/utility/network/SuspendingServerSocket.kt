package dev.crec.hawksbill.utility.network

import java.net.SocketAddress
import java.nio.channels.AsynchronousServerSocketChannel
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.CompletionHandler
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class SuspendingServerSocket(
    private val socketChannel: AsynchronousServerSocketChannel = AsynchronousServerSocketChannel.open()
) {
    suspend fun bind(address: SocketAddress) {
        suspendCoroutine<Unit> {
            socketChannel.bind(address)
        }
    }

    suspend fun close() {
        suspendCoroutine<Unit> {
            socketChannel.close()
        }
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
