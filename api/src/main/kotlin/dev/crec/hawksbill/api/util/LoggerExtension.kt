package dev.crec.hawksbill.api.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun Logger.child(name: String): Logger = LoggerFactory.getLogger("${this.name}|$name")
