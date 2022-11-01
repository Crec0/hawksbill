package dev.crec.hawksbill

import org.slf4j.Logger
import org.slf4j.LoggerFactory

val mainLogger: Logger = LoggerFactory.getLogger("HawksBill")

lateinit var bot: HawksBill
    private set

fun main() {
    bot = HawksBill()
}
