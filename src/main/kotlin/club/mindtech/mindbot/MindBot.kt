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

class MindBot(token: String) {
    companion object {
        lateinit var jda: JDA
        lateinit var db: MongoDatabase

        @JvmStatic
        fun main(args: Array<String>) {
            MindBot(env("DISCORD_TOKEN"))
        }
    }

    init {
        log.info("Initializing JDA...")
        jda = createJDA(token)
        log.info("JDA created")
        registerCommands()

        db = initDatabase(env("DB_URL"), env("DB_NAME"))
    }

    private fun createJDA(token: String): JDA {
        try {
            return JDABuilder.createDefault(token)
                .setEventManager(AnnotatedEventManager())
                .addEventListeners(InteractionListener())
                .build()
                .awaitReady()
        } catch (e: Exception) {
            log.error("Failed to create JDA: {}", e.message)
            exitProcess(1)
        }
    }

    private fun registerCommands() {
        val commandData = getSlashCommandData()
        jda.guilds.forEach {
            registerGuildCommands(it, commandData)
        }
    }

    private fun registerGuildCommands(guild: Guild, commandData: Array<CommandData>) {
        guild.updateCommands {
            addCommands(*commandData)
        }.queue()
    }
}
