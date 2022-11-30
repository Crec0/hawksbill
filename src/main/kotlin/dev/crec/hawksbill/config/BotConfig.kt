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

    @SerialName("debug")
    @TomlComment(
        """
            Setting it to true will enable debug level of logging and some development commands.
            OPTIONAL - Can be deleted
            """
    )
    val isDeveloperMode: Boolean = false,

    @SerialName("bridge-server-port")
    @TomlComment(
        """
        Port the bot will listen to for servers connecting for chat bridge
        DEFAULT: 25564
        REQUIRED
        """
    )
    val bridgePort: UShort = 25564.toUShort(),

    @SerialName("minecraft-server")
    @TomlComment(
        """
        List of minecraft server's you want to connect to via rcon.
        OPTIONAL - Can be deleted
        """
    )
    val minecraftServers: List<MinecraftServer> = listOf()
)

@Serializable
data class MinecraftServer(
    @SerialName("server-name")
    @TomlComment(
        """
        Name you want to assign to this server.
        Example: Survival, SMP, Creative, etc.
        """
    )
    val name: String,

    @SerialName("server-ip")
    @TomlComment(
        """
        Server ip player's use to connect
        Example: 127.0.0.1, 86.20.123.10
        """
    )
    val serverIp: String = "127.0.0.1",

    @SerialName("server-port")
    @TomlComment(
        """
        Server port use to connect if not default (25565)
        Example: 25565
        """
    )
    val serverPort: UShort = 25565.toUShort(),

    @SerialName("rcon-port")
    @TomlComment(
        """
        Rcon port of the server if not default (25575)
        Example: 25575
        """
    )
    val rconPort: UShort = 25575.toUShort(),

    @SerialName("rcon-password")
    @TomlComment(
        """
        Rcon password you set in server.properties
        Example: super-secret-password
        """
    )
    val rconPassword: String,

    @SerialName("bridge-port")
    @TomlComment(
        """
        Port the server will use for chat bridge.
        Default: 25576
        """
    )
    val bridgePort: UShort = 25576.toUShort(),

    @SerialName("bridge-channel-id")
    @TomlComment(
        """
        Channel id which will become the chat bridge between discord and mc server.
        """
    )
    val bridgeChannelId: String
)
