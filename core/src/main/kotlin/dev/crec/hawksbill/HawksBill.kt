package dev.crec.hawksbill

import com.mongodb.client.MongoDatabase
import dev.crec.hawksbill.api.annotation.Command
import dev.crec.hawksbill.api.command.ICommand
import dev.crec.hawksbill.database.initDatabase
import dev.crec.hawksbill.events.EventListener
import dev.crec.hawksbill.util.env
import dev.minn.jda.ktx.interactions.commands.updateCommands
import io.github.classgraph.ClassGraph
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.hooks.AnnotatedEventManager
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

val log = LoggerFactory.getLogger(HawksBill::class.java) as Logger

val commands: HashMap<String, ICommand> = HashMap()
val commandData: HashSet<CommandData> = HashSet()

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
    isDevelopment = args.contains("dev")
    populateCommands()
    log.info("${bot.jda.selfUser.name} is now online!")
}

fun populateCommands() {
    ClassGraph().enableClassInfo().enableAnnotationInfo().scan().use { result ->
        result.getClassesWithAnnotation(Command::class.java).forEach { classInfo ->
            val annotationValues = classInfo.getAnnotationInfo(Command::class.java).parameterValues
            val cmdClass = classInfo.loadClass().getDeclaredConstructor().newInstance() as ICommand
            commandData.add(
                Commands.slash(
                    annotationValues.getValue("name") as String,
                    annotationValues.getValue("description") as String
                )
            )
            commands[annotationValues.getValue("name") as String] = cmdClass
        }
    }
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
        api.guilds.forEach { guild ->
            guild.updateCommands { addCommands(commandData) }.queue()
        }
    }
}
