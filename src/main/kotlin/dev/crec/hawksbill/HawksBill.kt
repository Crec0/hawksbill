package dev.crec.hawksbill

import dev.crec.hawksbill.api.annotation.SlashCommandMarker
import dev.crec.hawksbill.api.command.ICommand
import dev.crec.hawksbill.api.config.ConfigIO
import dev.crec.hawksbill.impl.services.ReminderUpdatingService
import dev.crec.hawksbill.jda.EventListener
import dev.crec.hawksbill.utility.extensions.child
import dev.minn.jda.ktx.events.CoroutineEventManager
import io.github.classgraph.ClassGraph
import io.github.classgraph.ScanResult
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.events.session.ShutdownEvent
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import java.util.concurrent.CancellationException
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis
import kotlin.time.Duration.Companion.seconds

class HawksBill {
    val config = ConfigIO.read()
    val log = mainLogger.child("Bot")
    val database: CoroutineDatabase = KMongo.createClient(config.databaseURL).getDatabase(config.databaseName).coroutine
    val httpClient = OkHttpClient.Builder().connectionPool(ConnectionPool(10, 20, TimeUnit.SECONDS)).build()

    val reminderService = ReminderUpdatingService()

    val commands = scanCommands()
    val jda = initJDA()

    private fun scanCommands(): Map<String, ICommand> {
        val classGraph = ClassGraph()
            .enableClassInfo()
            .enableAnnotationInfo()
            .acceptPackages("dev.crec.hawksbill.impl.commands")

        val scanResult: ScanResult
        val timeTaken = measureTimeMillis {
            scanResult = classGraph.scan()
        }

        val classes = mutableListOf<ICommand>()

        scanResult.use { result ->
            log.info("Scanned ${result.allClasses.size} classes in $timeTaken ms")
            result.getClassesWithAnnotation(SlashCommandMarker::class.java).forEach { classInfo ->
                val cmdInstance = classInfo.loadClass().getConstructor().newInstance() as ICommand
                classes.add(cmdInstance)
            }
        }

        return classes.associateBy { it.name }
    }

    private fun updateCommands() {
        val commandData = commands.values.map { cmd -> cmd.commandData() }
        jda.guilds.forEach { guild ->
            guild.updateCommands().addCommands(commandData).queue()
        }
        log.info("Registered ${commandData.size} commands for ${jda.guilds.size} guild(s)")
    }

    private fun initJDA(): JDA {
        val supervisorJob= SupervisorJob()
        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            if (throwable !is CancellationException)
                log.error("Uncaught exception in coroutine", throwable)
            if (throwable is Error) {
                supervisorJob.cancel()
                throw throwable
            }
        }
        val scope = CoroutineScope(Dispatchers.IO + supervisorJob + exceptionHandler)
        val manager = CoroutineEventManager(scope, 30.seconds)

        manager.listener<ReadyEvent> {
            updateCommands()
        }

        manager.listener<ShutdownEvent> {
            supervisorJob.cancel()
        }

        scope.launch {
            reminderService.start(this)
        }

        return JDABuilder.createDefault(config.token)
            .setEventManager(manager)
            .addEventListeners(EventListener())
            .setHttpClient(httpClient)
            .build()
    }
}
