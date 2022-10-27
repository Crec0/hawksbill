package dev.crec.hawksbill.api.util

import java.security.MessageDigest
import kotlin.random.Random

const val EMPTY = "\u180E"
const val SPACE_1EM = "\u2003"

private const val HEX = "0123456789abcdef"
private const val RANDOM_BYTES_LIMIT = 32

fun String.truncate(limit: Int) = "${this.substring(0, kotlin.math.max(0, kotlin.math.min(this.length, limit)))}..."


fun String.hashString(algorithm: String) = MessageDigest.getInstance(algorithm)
    .digest(
        "$this${Random.nextBytes(RANDOM_BYTES_LIMIT).joinToString(separator = "")}".toByteArray()
    )
    .map { it.toInt() }
    .joinToString(separator = "") { byte -> "${HEX[byte shr 4 and 0x0F]}${HEX[byte and 0x0F]}" }


fun StringBuilder.newLine(): StringBuilder {
    this.append("\n")
    return this
}
