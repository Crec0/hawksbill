package club.mindtech.mindbot.util

const val gap = "⠀"

fun bold(text: String): String = "**${text.trim()}**"

fun italic(text: String): String = "*${text.trim()}*"

fun underline(text: String): String = "__${text.trim()}__"

fun code(text: String): String = "`${text.trim()}`"

fun stringify(strings: Set<String>): String = strings.toString().replace("\\[]".toRegex(), "")

fun zFill(number: Int, length: Int): String = "$number".padStart(length, '0')
