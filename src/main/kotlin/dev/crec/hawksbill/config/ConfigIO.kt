package dev.crec.hawksbill.config

import dev.crec.hawksbill.utility.extensions.toPath
import kotlinx.serialization.decodeFromString
import net.peanuuutz.tomlkt.Toml
import kotlin.io.path.name
import kotlin.io.path.notExists
import kotlin.io.path.readText
import kotlin.io.path.writeText

object ConfigIO {
    private val sampleConfig = BotConfig(
        token = "-------",
        tenorApiKey = "-------",
        databaseURL = "mongodb+srv://-------",
        databaseName = "HawksBill-Bot",
        minecraftServers = listOf(
            MinecraftServer(
                name = "adventure",
                rconPassword = "super-secret",
                bridgeChannelId = "0198019801980912"
            )
        )
    )

    private val configPath = "./config.toml".toPath()
    private val sampleConfigPath = "./sample.config.toml".toPath()

    fun read(): BotConfig {
        if (configPath.notExists()) {
            writeDefault()
            throw IllegalStateException(
                "Config file doesn't exist at the location. Created ${sampleConfigPath.name} with default config."
            )
        }
        return Toml.decodeFromString(configPath.readText())
    }

    private fun writeDefault() {
        sampleConfigPath.writeText(Toml.encodeToString(BotConfig.serializer(), sampleConfig))
    }
}
