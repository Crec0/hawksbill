package dev.crec.hawksbill.impl.services

import dev.crec.hawksbill.api.services.RepeatingService
import dev.crec.hawksbill.bot
import dev.crec.hawksbill.impl.database.ReminderDTO
import dev.crec.hawksbill.utility.extensions.asExpiredValuesFlow
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.generics.getChannel
import kotlinx.coroutines.CoroutineScope
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import org.litote.kmongo.`in`
import org.litote.kmongo.lt
import java.time.Instant
import java.util.concurrent.DelayQueue
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class ReminderUpdatingService {
    private var cache: DelayQueue<ReminderDTO> = DelayQueue()
    private var coolDown = 0.seconds

    private val requiresUpdate = AtomicBoolean(false)

    private val collection
        get() = bot.database.getCollection<ReminderDTO>()

    fun requestUpdate() {
        this.requiresUpdate.set(true)
    }

    private suspend fun updateCache() {
        if (coolDown.inWholeSeconds == 0L || requiresUpdate.get()) {
            requiresUpdate.set(false)

            coolDown = 60.seconds

            collection
                .find(ReminderDTO::expiry lt (Instant.now().epochSecond + 5.minutes.inWholeSeconds))
                .toList()
                .forEach {
                    if (!cache.contains(it)) {
                        cache.add(it)
                    }
                }
        } else {
            coolDown -= 1.seconds
        }
    }

    private suspend fun servicePartial(scope: CoroutineScope): RepeatingService {
        return RepeatingService(5.seconds, 1.seconds, scope) {
            updateCache()
            val remindersToDelete = mutableListOf<String>()

            cache.asExpiredValuesFlow().collect { reminder ->
                val channel = bot.jda.getChannel<TextChannel>(reminder.channel_id)
                val member = channel?.guild?.retrieveMemberById(reminder.member_id)?.await()

                if (channel == null || member != null && !channel.canTalk(member)) {
                    bot.jda.openPrivateChannelById(reminder.member_id)
                        .flatMap { it.sendMessage(reminder.formattedMessage) }
                        .queue()
                } else {
                    channel.sendMessage(reminder.formattedMessage).queue()
                }

                remindersToDelete.add(reminder.reminder_id)
            }

            if (remindersToDelete.isNotEmpty()) {
                collection.deleteMany(ReminderDTO::reminder_id `in` remindersToDelete)
            }
        }
    }

    suspend fun start(scope: CoroutineScope) {
        servicePartial(scope).start()
    }
}
