package dev.crec.hawksbill

import dev.crec.hawksbill.api.services.Service
import dev.crec.hawksbill.impl.services.ReminderUpdatingService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ServiceManager {

    val services = listOf<Service>(
        ReminderUpdatingService()
    ).associateBy { service ->
        service::class.qualifiedName
            ?: throw IllegalStateException("Null class name for service")
    }

    fun startServices(scope: CoroutineScope) {
        services.values.forEach { service ->
            scope.launch { service.start() }
        }
    }
}
