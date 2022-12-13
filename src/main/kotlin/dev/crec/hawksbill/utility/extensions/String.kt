package dev.crec.hawksbill.utility.extensions

import java.nio.file.Path
import java.security.MessageDigest
import kotlin.math.min
import kotlin.math.max
import kotlin.random.Random

const val EMPTY = "\u180E"
const val SPACE_1EM = "\u2003"
const val NBSP = "\u00A0"

private const val HEX = "0123456789abcdef"
private const val RANDOM_BYTES_LIMIT = 32

fun String.truncate(limit: Int) = "${this.substring(0, max(0, min(this.length, limit)))}..."

fun String.toPath() = Path.of(this)

fun String.hashString(algorithm: String) = MessageDigest.getInstance(algorithm)
    .digest("$this${Random.nextBytes(RANDOM_BYTES_LIMIT).joinToString(separator = "")}".toByteArray())
    .map { it.toInt() }
    .joinToString(separator = "") { byte ->
        "${HEX[byte shr 4 and 0x0F]}${HEX[byte and 0x0F]}"
    }


fun StringBuilder.newLine(): StringBuilder {
    this.append("\n")
    return this
}

fun alignStringPair(pair: Pair<String, String>, widthOfFirst: Int) =
    "${pair.first}${NBSP.repeat(widthOfFirst + 5)}${pair.second}"
