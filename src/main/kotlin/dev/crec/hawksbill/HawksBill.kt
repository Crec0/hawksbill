package dev.crec.hawksbill

import dev.crec.hawksbill.api.annotation.SlashCommandMarker
import dev.crec.hawksbill.api.command.ICommand
import dev.crec.hawksbill.api.config.ConfigIO
import dev.crec.hawksbill.jda.EventListener
import dev.crec.hawksbill.utility.extensions.child
import dev.minn.jda.ktx.events.CoroutineEventManager
import io.github.classgraph.ClassGraph
import io.github.classgraph.ScanResult
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.events.session.ShutdownEvent
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import org.litote.kmongo.KMongo
import java.util.concurrent.CancellationException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.system.measureTimeMillis
import kotlin.time.Duration.Companion.minutes

class HawksBill {
    private val threadPool = Executors.newScheduledThreadPool(2) {
        thread(start = false, name = "HawksBill-Worker-Thread", isDaemon = true, block = it::run)
    }

    val config = ConfigIO.read()
    val log = mainLogger.child("Bot")

    val commands = initCommands()
    val db = initMongoDB()
    val httpClient = initHttpClient()
    val jda = initJDA()

    val manager = jda.eventManager as CoroutineEventManager

    private fun initMongoDB() = KMongo.createClient(config.databaseURL).getDatabase(config.databaseName)

    private fun initHttpClient() = OkHttpClient.Builder()
        .connectionPool(ConnectionPool(10, 20, TimeUnit.SECONDS))
        .build()

    private fun initJDA(): JDA {
        val dispatcher = threadPool.asCoroutineDispatcher()
        val supervisor = SupervisorJob()
        val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
            if (throwable !is CancellationException)
                log.error("Uncaught exception in coroutine", throwable)
            if (throwable is Error) {
                supervisor.cancel()
                throw throwable
            }
        }
        val context = dispatcher + supervisor + coroutineExceptionHandler
        val scope = CoroutineScope(context)

        val manager = CoroutineEventManager(scope, 1.minutes)

        manager.listener<ReadyEvent> {
            updateCommands()
        }

        manager.listener<ShutdownEvent> {
            supervisor.cancel()
        }

        return JDABuilder.createDefault(config.token)
            .setEventManager(manager)
            .addEventListeners(EventListener())
            .setHttpClient(httpClient)
            .setCallbackPool(threadPool)
            .setGatewayPool(threadPool)
            .setRateLimitPool(threadPool)
            .build()
    }

    private fun initCommands(): Map<String, ICommand> {
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
}
