package dev.crec.hawksbill.impl.services

import dev.crec.hawksbill.api.services.Service
import dev.crec.hawksbill.bot
import dev.minn.jda.ktx.jdabuilder.scope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration

sealed class RepeatingService(
    private val startDelay: Duration,
    private val delay: Duration,
) : Service {

    override suspend fun runTask(task: suspend () -> Unit) {
        bot.jda.scope.launch {
            delay(startDelay)
            while (true) {
                task()
                delay(delay)
            }
        }
    }
}
