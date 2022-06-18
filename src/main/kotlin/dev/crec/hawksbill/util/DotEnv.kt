package dev.crec.hawksbill.util

import dev.crec.hawksbill.log
import java.io.File

private val envVars: MutableMap<String, String> = mutableMapOf()

fun readEnvFile() {
    if (envVars.isNotEmpty()) return

    File(".env").bufferedReader().forEachLine {
        if (it.isBlank() || it.startsWith("#")) return@forEachLine

        if (!it.contains("=")) {
            log.warn("Invalid line in .env file: $it")
            return@forEachLine
        }

        val keyValPairs = it.split("=", limit = 2).map { part -> part.trim() }

        keyValPairs.any { part -> part.isBlank() }.let { isBlank ->
            if (isBlank) {
                log.warn("Invalid line in .env file: $it")
                return@forEachLine
            }
        }

        envVars[keyValPairs[0]] = keyValPairs[1]
    }
}

fun envOrNull(key: String): String? {
    readEnvFile()
    return envVars[key]
}

fun env(key: String): String {
    return envOrNull(key) ?: throw IllegalStateException("Environment variable $key is not set")
}
