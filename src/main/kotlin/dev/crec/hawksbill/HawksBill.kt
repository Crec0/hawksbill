package dev.crec.hawksbill

import com.mongodb.client.MongoDatabase
import dev.crec.hawksbill.commands.getSlashCommandData
import dev.crec.hawksbill.database.initDatabase
import dev.crec.hawksbill.events.EventListener
import dev.crec.hawksbill.util.env
import dev.minn.jda.ktx.interactions.commands.updateCommands
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.hooks.AnnotatedEventManager
import org.mariuszgromada.math.mxparser.mathcollection.BooleanAlgebra.T
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

val log: Logger = LoggerFactory.getLogger(HawksBill::class.java)

val bot by lazy {
    HawksBill(
        token = env("DISCORD_TOKEN"),
        dbURI = env("DB_URL"),
        dbName = env("DB_NAME")
    )
}

private var isDevelopment = false

fun isDevelopment() = isDevelopment

fun main(vararg args: String) {
    println(isDevelopment())
    isDevelopment = args.contains("dev")
    println(isDevelopment())
    log.info("${bot.jda.selfUser.name} is now online!")
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

class HawksBill(token: String, dbURI: String, dbName: String) {
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
