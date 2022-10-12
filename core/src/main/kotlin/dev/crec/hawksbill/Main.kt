package dev.crec.hawksbill

import dev.crec.hawksbill.api.HawksBill
import dev.crec.hawksbill.api.annotation.SlashCommandMarker
import dev.crec.hawksbill.api.command.ICommand
import dev.crec.hawksbill.api.util.Env
import dev.crec.hawksbill.events.EventListener
import io.github.classgraph.ClassGraph
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.hooks.AnnotatedEventManager
import org.litote.kmongo.KMongo

val isDev = Env["ENVIRONMENT"].lowercase() == "dev"

private fun populateCommands(): Set<ICommand> {
    val commands = mutableSetOf<ICommand>()
    ClassGraph()
        .enableClassInfo()
        .enableAnnotationInfo()
        .scan()
        .use { result ->
            result.getClassesWithAnnotation(SlashCommandMarker::class.java).forEach { classInfo ->
                val cmdInstance = classInfo.loadClass().getConstructor().newInstance() as ICommand
                commands.add(cmdInstance)
            }
        }
    return commands
}

fun main() {

    HawksBill.registerCommands(populateCommands())

    HawksBill.jda = JDABuilder.createDefault(Env["DISCORD_TOKEN"])
        .setEventManager(AnnotatedEventManager())
        .addEventListeners(EventListener())
        .build()

    HawksBill.database = KMongo.createClient(Env["DB_URL"]).getDatabase(Env["DB_NAME"])

    HawksBill.logger.info("${HawksBill.jda.selfUser.name} is now online!")
}
