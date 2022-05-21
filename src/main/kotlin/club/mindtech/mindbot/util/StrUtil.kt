package club.mindtech.mindbot.util

import java.security.MessageDigest
import kotlin.random.Random

const val EMPTY = "\u180E"
const val SPACE_1EM = "\u2003"

private const val HEX = "0123456789abcdef"
private const val RANDOM_BYTES_LIMIT = 32

fun zFill(number: Int, length: Int): String = "$number".padStart(length, '0')

fun String.truncate(limit: Int) = if (this.length > limit) "${this.substring(0, limit)}..." else this

fun String.hashString(algorithm: String) = MessageDigest.getInstance(algorithm)
    .digest(
        "$this${Random.nextBytes(RANDOM_BYTES_LIMIT).joinToString(separator = "")}".toByteArray()
    )
    .map { it.toInt() }
    .joinToString(separator = "") { byte ->
        "${HEX[byte shr 4 and 0x0F]}${HEX[byte and 0x0F]}"
    }
