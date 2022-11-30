package dev.crec.hawksbill.config

import dev.crec.hawksbill.utility.extensions.toPath
import kotlinx.serialization.decodeFromString
import net.peanuuutz.tomlkt.Toml
import kotlin.io.path.name
import kotlin.io.path.notExists
import kotlin.io.path.readText
import kotlin.io.path.writeText

object ConfigIO {
    private val defaultConfig = BotConfig(
        token = "-------",
        tenorApiKey = "-------",
        databaseURL = "mongodb+srv://..........",
        databaseName = "HawksBill-Bot",
        rconClients = listOf(
            RconClient(
                name = "adventure",
                ip = "69.69.69.69",
                port = 25565,
                password = "super-secret",
                channelId = "0198019801980912"
            )
        )
    )

    private val configPath = "./config.toml".toPath()

    fun read(): BotConfig {
        if (configPath.notExists()) {
            writeDefault()
            throw IllegalStateException(
                "Config file doesn't exist at the location. Created ${configPath.name} with default config."
            )
        }
        return Toml.decodeFromString(configPath.readText())
    }

    private fun writeDefault() {
        configPath.writeText(Toml.encodeToString(BotConfig.serializer(), defaultConfig))
    }
}
