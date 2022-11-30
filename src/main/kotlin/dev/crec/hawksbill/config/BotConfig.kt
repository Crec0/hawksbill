package dev.crec.hawksbill.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.peanuuutz.tomlkt.TomlComment

@Serializable
data class BotConfig(
    @SerialName("bot-token")
    @TomlComment(
        """
        Discord bot token.
        Get from https://discord.com/developers/applications
        REQUIRED
        """
    )
    val token: String,

    @SerialName("tenor-api-key")
    @TomlComment(
        """
        Tenor api key for gif search.
        Get from https://developers.google.com/tenor/guides/quickstart#setup 
        REQUIRED
        """
    )
    val tenorApiKey: String,

    @SerialName("mongo-database-url")
    @TomlComment(
        """
        MongoDB server url 
        REQUIRED
        """
    )
    val databaseURL: String,

    @SerialName("mongo-database-name")
    @TomlComment(
        """
        MongoDB database name 
        REQUIRED
        """
    )
    val databaseName: String,

    @SerialName("rcon-port")
    @TomlComment(
        """
        Port the bot will listen to for servers connecting
        REQUIRED
        """
    )
    val rconPort: UShort = 25560.toUShort(),

    @SerialName("debug")
    @TomlComment(
        """
            Setting it to true will enable debug level of logging, which may be overwhelming for regular use.
            OPTIONAL - Can be deleted
            """
    )
    val isDebugEnabled: Boolean = false,

    @SerialName("rcon-client")
    @TomlComment(
        """
        List of minecraft server's you want to connect to via rcon.
        OPTIONAL - Can be deleted
        """
    )
    val rconClients: List<RconClient> = listOf()
)

@Serializable
data class RconClient(
    @SerialName("server-name")
    @TomlComment(
        """
        Name you want to assign to this server.
        Example: Survival, SMP, Creative, etc.
        """
    )
    val name: String,

    @SerialName("server-port")
    @TomlComment(
        """
        Server ip player's use to connect
        Example: Survival, SMP, Creative, etc.
        """
    )
    val ip: String,

    @SerialName("rcon-port")
    @TomlComment(
        """
        Rcon port you set in server.properties
        Example: 25575
        """
    )
    val port: Int,

    @SerialName("rcon-password")
    @TomlComment(
        """
        Rcon password you set in server.properties
        Example: super-secret-password
        """
    )
    val password: String,

    @SerialName("bridge-channel")
    @TomlComment(
        """
        Channel id which will become the chat bridge between discord and mc server.
        """
    )
    val channelId: String
)
