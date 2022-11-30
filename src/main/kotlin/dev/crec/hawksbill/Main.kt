package dev.crec.hawksbill

import org.slf4j.Logger
import org.slf4j.LoggerFactory

val mainLogger: Logger = LoggerFactory.getLogger("HawksBill")

val bot by lazy {
    HawksBill()
}

fun main() {
    bot.init()
}
