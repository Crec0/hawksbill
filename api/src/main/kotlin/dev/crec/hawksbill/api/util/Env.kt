package dev.crec.hawksbill.api.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

object Env {
    private val log: Logger = LoggerFactory.getLogger("DotEnv")
    private val envVars: MutableMap<String, String> = mutableMapOf()

    private fun readEnvFile() {
        if (envVars.isNotEmpty()) return

        File("../.env").bufferedReader().forEachLine { line ->
            if (line.isBlank() || line.startsWith("#")) return@forEachLine

            if (!line.contains("=")) {
                log.warn("Invalid line in .env file: $line")
                return@forEachLine
            }

            val keyValPairs = line.split("=", limit = 2).map { part -> part.trim() }

            keyValPairs.any { part -> part.isBlank() }.let { isBlank ->
                if (isBlank) {
                    log.warn("Invalid line in .env file: $line")
                    return@forEachLine
                }
            }

            envVars[keyValPairs[0]] = keyValPairs[1]
        }
    }

    operator fun get(key: String): String {
        readEnvFile()
        return envVars[key] ?: throw IllegalStateException("Environment variable $key is not set")
    }
}

