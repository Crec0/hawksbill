package dev.crec.hawksbill.api.services

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration

class RepeatingService(
    private val startDelay: Duration,
    private val delay: Duration,
    private val scope: CoroutineScope,
    private val task: suspend CoroutineScope.() -> Unit
) : Service {

    override suspend fun start() {
        scope.launch {
            delay(startDelay)
            while (true) {
                task()
                delay(delay)
            }
        }
    }
}
