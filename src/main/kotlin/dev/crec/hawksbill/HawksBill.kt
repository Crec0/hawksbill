package dev.crec.hawksbill

import dev.crec.hawksbill.api.annotation.SlashCommandMarker
import dev.crec.hawksbill.api.command.ICommand
import dev.crec.hawksbill.api.database.Entity
import dev.crec.hawksbill.api.services.Service
import dev.crec.hawksbill.config.ConfigIO
import dev.crec.hawksbill.impl.jda.EventListener
import dev.crec.hawksbill.utility.requests.TenorAPI
import dev.minn.jda.ktx.events.CoroutineEventManager
import io.github.classgraph.ClassGraph
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import mu.KotlinLogging
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import java.util.concurrent.CancellationException
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates.notNull
import kotlin.time.Duration.Companion.seconds

class HawksBill {

    val log = KotlinLogging.logger {}

    val config = ConfigIO.read()
    val tenorAPI = TenorAPI(config)
    val database = KMongo.createClient(config.databaseURL).getDatabase(config.databaseName).coroutine
    val httpClient = OkHttpClient.Builder().connectionPool(ConnectionPool(10, 20, TimeUnit.SECONDS)).build()

    var coroutineScope: CoroutineScope by notNull()
        private set

    var serviceManager: ServiceManager by notNull()
        private set

    var commands: Map<String, ICommand> by notNull()
        private set

    var jda: JDA by notNull()
        private set

    fun init() {
        log.info { "[Init] Init" }
        log.info { "[Init] Command Scanning" }
        commands = instantiateCommands()
        log.info { "[Done] Command Scanning" }

        log.info { "[Init] JDA" }
        coroutineScope = buildCoroutineScope()
        jda = instantiateJDA()
        log.info { "[Done] JDA" }

        log.info { "[Init] Services" }
        serviceManager = ServiceManager().apply {
            startServices(coroutineScope)
        }
        log.info { "[Done] Services" }
        log.info { "[Done] Init" }
    }

    inline fun <reified TEntity : Entity> mongoCollection() = database.getCollection<TEntity>()

    inline fun <reified TService: Service> service(): TService {
        val service = serviceManager.services[TService::class.qualifiedName]
        return checkNotNull(service) { "Service for class ${TService::class} doesn't exist." } as TService
    }

    private fun buildCoroutineScope(): CoroutineScope {
        val ancestorJob = SupervisorJob()

        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            if (throwable !is CancellationException) {
                log.error(throwable) { "Uncaught exception in coroutine" }
            }

            if (throwable is Error) {
                ancestorJob.cancel()
                throw throwable
            }
        }

        return CoroutineScope(Dispatchers.IO + ancestorJob + exceptionHandler)
    }

    private fun instantiateCommands(): Map<String, ICommand> {
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

        log.debug {
            "Commands Scanned => ${classes.map { it.name }.sorted()}"
        }
        return classes.associateBy { it.name }
    }

    private fun instantiateJDA(): JDA {
        return JDABuilder.createDefault(config.token)
            .setEventManager(CoroutineEventManager(coroutineScope, 5.seconds))
            .addEventListeners(EventListener())
            .setHttpClient(httpClient)
            .build()
    }
}
