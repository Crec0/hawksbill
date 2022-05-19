package club.mindtech.mindbot

import club.mindtech.mindbot.commands.getSlashCommandData
import club.mindtech.mindbot.database.initDatabase
import club.mindtech.mindbot.events.EventListener
import club.mindtech.mindbot.util.env
import com.mongodb.client.MongoDatabase
import dev.minn.jda.ktx.interactions.commands.updateCommands
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.hooks.AnnotatedEventManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

val log: Logger = LoggerFactory.getLogger(MindBot::class.java)

val bot = MindBot(
    token = env("DISCORD_TOKEN"),
    dbURI = env("DB_URL"),
    dbName = env("DB_NAME")
)

fun main() {}

inline fun <reified T : Any> initOrExit(block: () -> T): T {
    log.info("Initializing ${T::class.simpleName}")
    return try {
        block()
    } catch (e: Exception) {
        log.error("Fatal exception", e)
        exitProcess(1)
    }
}

class MindBot(token: String, dbURI: String, dbName: String) {
    val jda: JDA = initOrExit { initJDA(token) }
    val database: MongoDatabase = initOrExit { initDatabase(dbURI, dbName) }

    private fun initJDA(token: String): JDA {
        val jda = JDABuilder.createDefault(token)
            .setEventManager(AnnotatedEventManager())
            .addEventListeners(EventListener())
            .build()

        log.info("JDA initialized")

        return jda
    }

    fun registerCommands(api: JDA) {
        getSlashCommandData().let { commandData ->
            api.guilds.forEach { guild ->
                guild.updateCommands { addCommands(commandData) }.queue()
            }
        }
    }
}
