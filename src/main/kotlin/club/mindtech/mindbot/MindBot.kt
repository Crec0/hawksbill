package club.mindtech.mindbot

import club.mindtech.mindbot.commands.getSlashCommandData
import club.mindtech.mindbot.database.initDatabase
import club.mindtech.mindbot.events.InteractionListener
import club.mindtech.mindbot.util.env
import com.mongodb.client.MongoDatabase
import dev.minn.jda.ktx.interactions.commands.updateCommands
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.hooks.AnnotatedEventManager
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

val log: Logger = LoggerFactory.getLogger(MindBot::class.java)

val bot: MindBot by lazy {
    MindBot(
        token = env("DISCORD_TOKEN"),
        dbURI = env("DB_URL"),
        dbName = env("DB_NAME")
    )
}

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
            .addEventListeners(InteractionListener())
            .build()
            .awaitReady()
        log.info("JDA initialized")

        return jda.also {
            registerCommands(it)
            log.info("Commands registered")
        }
    }

    private fun registerCommands(api: JDA) {
        val commandData = getSlashCommandData()
        api.guilds.forEach {
            registerGuildCommands(it, commandData)
        }
    }

    private fun registerGuildCommands(guild: Guild, commandData: Array<CommandData>) {
        guild.updateCommands {
            addCommands(*commandData)
        }.queue()
    }
}

fun main() {
    bot.jda.restPing.queue { time ->
        log.info("Gateway ping: $time")
    }
}
