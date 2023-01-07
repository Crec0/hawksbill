package dev.crec.hawksbill

import dev.crec.hawksbill.api.annotation.SlashCommandMarker
import dev.crec.hawksbill.api.command.ICommand
import dev.crec.hawksbill.api.database.Entity
import dev.crec.hawksbill.config.ConfigIO
import dev.crec.hawksbill.impl.jda.EventListener
import dev.crec.hawksbill.impl.services.ReminderUpdatingService
import dev.crec.hawksbill.utility.extensions.child
import dev.crec.hawksbill.utility.requests.TenorAPI
import dev.minn.jda.ktx.events.CoroutineEventManager
import dev.minn.jda.ktx.jdabuilder.scope
import io.github.classgraph.ClassGraph
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
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import java.util.concurrent.CancellationException
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates
import kotlin.time.Duration.Companion.seconds

class HawksBill {
    val log        = mainLogger.child("Bot")
    val config     = ConfigIO.read()
    val database   = KMongo.createClient(config.databaseURL).getDatabase(config.databaseName).coroutine
    val httpClient = OkHttpClient.Builder().connectionPool(ConnectionPool(10, 20, TimeUnit.SECONDS)).build()

    val reminderService = ReminderUpdatingService()

    val tenorAPI = TenorAPI(config)

    var commands: Map<String, ICommand> by Delegates.notNull()
        private set

    var jda: JDA by Delegates.notNull()
        private set

    fun init() {
        commands = scanCommands()
        jda = initJDA()

        jda.scope.launch {
            reminderService.start()
        }
    }

    inline fun <reified C: Entity> mongoCollection() = database.getCollection<C>()

    private fun scanCommands(): Map<String, ICommand> {
        val classGraph = ClassGraph()
            .enableClassInfo()
            .enableAnnotationInfo()
            .acceptPackages("dev.crec.hawksbill.impl.commands")

        val classes = mutableListOf<ICommand>()

        classGraph.scan().use { result ->
            result.getClassesWithAnnotation(SlashCommandMarker::class.java).forEach { classInfo ->
                val cmdInstance = classInfo.loadClass().getConstructor().newInstance() as ICommand
                classes.add(cmdInstance)
            }
        }

        return classes.associateBy { it.name }
    }

    private fun updateCommands() {
        val commandData = commands.values.map(ICommand::commandData)
        jda.updateCommands().addCommands(commandData).queue()
        log.info("Registered ${commandData.size} commands.")
    }

    private fun initJDA(): JDA {
        val supervisorJob = SupervisorJob()
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

        return JDABuilder.createDefault(config.token)
            .setEventManager(manager)
            .addEventListeners(EventListener())
            .setHttpClient(httpClient)
            .build()
    }
}
