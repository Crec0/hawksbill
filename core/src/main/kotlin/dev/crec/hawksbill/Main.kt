package dev.crec.hawksbill

import dev.crec.hawksbill.api.HawksBill
import dev.crec.hawksbill.api.annotation.SlashCommandMarker
import dev.crec.hawksbill.api.command.ICommand
import dev.crec.hawksbill.api.util.Env
import dev.crec.hawksbill.events.EventListener
import io.github.classgraph.ClassGraph
import io.github.classgraph.ScanResult
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.hooks.AnnotatedEventManager
import org.litote.kmongo.KMongo
import org.slf4j.LoggerFactory
import kotlin.system.measureTimeMillis

private val log = LoggerFactory.getLogger("${HawksBill.name}|Main")

private fun populateCommands(): Set<ICommand> {
    val commands = mutableSetOf<ICommand>()
    val classGraph = ClassGraph()

    classGraph.apply {
        enableClassInfo()
        enableAnnotationInfo()
    }

    val scanResult: ScanResult
    val timeTaken = measureTimeMillis { scanResult = classGraph.scan() }

    scanResult.use { result ->
        log.info("Scanned ${result.allClasses.size} classes in $timeTaken ms")
        result.getClassesWithAnnotation(SlashCommandMarker::class.java).forEach { classInfo ->
            val cmdInstance = classInfo.loadClass().getConstructor().newInstance() as ICommand
            commands.add(cmdInstance)
        }
    }
    return commands
}

fun main() {
    log.info("Initializing Hawksbill")
    log.info("Populating commands")

    HawksBill.registerCommands(populateCommands())

    log.info("${HawksBill.commands.size} commands populated")
    log.info("Initializing events")


    HawksBill.jda = JDABuilder.createDefault(Env.get("DISCORD_TOKEN"))
        .setEventManager(AnnotatedEventManager())
        .addEventListeners(EventListener())
        .build()

    log.info("Events initialized")
    log.info("Connecting to Database")

    HawksBill.database = KMongo.createClient(Env.get("DB_URL")).getDatabase(Env.get("DB_NAME"))

    log.info("Database connected")
    log.info("${HawksBill.jda.selfUser.name} is now online!")
}
