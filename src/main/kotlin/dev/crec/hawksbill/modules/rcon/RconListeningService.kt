package dev.crec.hawksbill.modules.rcon

//import dev.crec.hawksbill.api.services.Service
//import dev.crec.hawksbill.bot
//import dev.crec.hawksbill.utility.network.SuspendingServerSocket
//import dev.crec.hawksbill.utility.network.SuspendingSocket
//import net.dv8tion.jda.api.entities.Message.MentionType
//import java.nio.ByteBuffer
//import java.nio.ByteOrder
//import java.nio.charset.StandardCharsets
//
//private val escapedPattern = Regex("\\\\([_*].*)") to "$1"
//
//private val formattingMap = listOf(
//    Regex("(?<![\\\\\\[])\\*\\*(.*)\\*\\*") to "§l$1§7", // bold
//    Regex("(?<![\\\\\\[])__(.*)__") to "§n$1§7", // underline
//    Regex("(?<![\\\\\\[])\\*(.*)\\*") to "§o$1§7", // italic
//    Regex("(?<![\\\\\\[])_([^_ ]+)_") to "§o$1§7", // italic
//    Regex("(?<![\\\\\\[])~~(.*)~~") to "§m$1§7", // strike through
//)
//
//private fun sanitize(message: String): String {
//    var sanitizedMessage = message
//    formattingMap.forEach { pair ->
//        sanitizedMessage = pair.first.replace(sanitizedMessage, pair.second)
//    }
//    while (escapedPattern.first.containsMatchIn(sanitizedMessage)) {
//        sanitizedMessage = escapedPattern.first.replace(sanitizedMessage, escapedPattern.second)
//    }
//    return sanitizedMessage
//}
//
//suspend fun chatMessage(message: String) {
//    login()
//    sanitize(message).chunked(MAX_WRITE_LENGTH).forEach { chunk ->
//        send(
//            RconPacket(
//                requestCounter.incrementAndGet(),
//                RconResponse.CHAT_BRIDGE,
//                chunk.toByteArray()
//            )
//        )
//    }
//}
//
//class RconListeningService: Service {
//    private val serverSocket = SuspendingServerSocket()
//
//    suspend fun startConnection() {
//        while (true) {
//            try {
//                val socket = serverSocket.accept()
//                if (!socket.remoteAddress.isSiteLocalAddress && !socket.remoteAddress.isLoopbackAddress) {
//                    continue
//                }
//                read(socket)
//            } catch (ignored: Exception) {
//            }
//        }
//    }
//
//    private fun replaceEmotes(message: String): String {
//        return message.replace(":(\\w+):".toRegex()) {
//            val emoteName = it.groups[1]!!.value
//            val candidateEmotes = bot.jda.getEmojisByName(emoteName, false)
//            return@replace if (candidateEmotes.size > 0) {
//                candidateEmotes[0].asMention
//            } else {
//                ":$emoteName:"
//            }
//        }
//    }
//
//    private suspend fun read(socket: SuspendingSocket) {
//        if (socket.isClosed) return
//
//        val bufferArray = ByteArray(MAX_READ_LENGTH)
//        val buffer = ByteBuffer.wrap(bufferArray).order(ByteOrder.LITTLE_ENDIAN)
//
//        val bytesRead = socket.read(buffer)
//
//        if (bytesRead < 10) return
//
//        val size = buffer.int
//
//        // If payload size is 0, we ignore
//        val payloadSize = size - Integer.BYTES - Byte.SIZE_BYTES * 2
//        if (payloadSize <= 0) return
//
//        val type = when (buffer.int) {
//            69 -> RconResponse.CHAT_BRIDGE
//            else -> RconResponse.UNKNOWN
//        }
//        if (type == RconResponse.UNKNOWN) return
//
//        val payload = ByteArray(payloadSize)
//        buffer.get(payload, 0, payloadSize)
//        buffer.clear()
//
//        val message = payload.toString(StandardCharsets.UTF_8)
//
//        val channel = bot.jda.getTextChannelById(env("CHAT_BRIDGE_CHANNEL")) ?: return
//        channel.sendMessage(replaceEmotes(message)).setAllowedMentions(listOf(MentionType.USER)).queue()
//    }
//
//    override suspend fun runTask(task: suspend () -> Unit) {
//        TODO("Not yet implemented")
//    }
//}
