package dev.crec.hawksbill.utility.extensions

import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun Logger.child(name: String): Logger = LoggerFactory.getLogger("${this.name}|$name")
