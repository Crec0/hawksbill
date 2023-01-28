package dev.crec.hawksbill.impl.services

import dev.crec.hawksbill.api.services.Service
import kotlinx.coroutines.delay
import kotlin.time.Duration

sealed class RepeatingService(
    private val startDelay: Duration,
    private val deltaDelay: Duration
): Service {

    override suspend fun start() {
        delay(startDelay)
        while (true) {
            task()
            delay(deltaDelay)
        }
    }
}
