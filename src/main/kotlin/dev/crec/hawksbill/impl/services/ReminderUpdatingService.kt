package dev.crec.hawksbill.impl.services

import dev.crec.hawksbill.bot
import dev.crec.hawksbill.impl.database.ReminderDTO
import dev.crec.hawksbill.utility.extensions.asExpiredValuesFlow
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.generics.getChannel
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import org.litote.kmongo.`in`
import org.litote.kmongo.lt
import java.time.Instant
import java.util.concurrent.DelayQueue
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class ReminderUpdatingService : RepeatingService(5.seconds, 1.seconds) {

    private var cache: DelayQueue<ReminderDTO> = DelayQueue()
    private var coolDown = 0.seconds

    private val requiresUpdate = AtomicBoolean(false)

    fun requestUpdate() {
        this.requiresUpdate.set(true)
    }

    private suspend fun updateCache() {
        if (coolDown.inWholeSeconds == 0L || requiresUpdate.get()) {

            requiresUpdate.set(false)
            coolDown = 60.seconds

            val fiveMinutesInFuture = Instant.now().epochSecond + 5.minutes.inWholeSeconds

            bot.mongoCollection<ReminderDTO>()
                .find(ReminderDTO::expiry lt fiveMinutesInFuture)
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

    override suspend fun task() {
        updateCache()
        val remindersToDelete = mutableListOf<String>()

        cache.asExpiredValuesFlow().collect { reminder ->
            val channel = bot.jda.getChannel<TextChannel>(reminder.channelId)
            val member = channel?.guild?.retrieveMemberById(reminder.memberId)?.await()

            if (channel == null || member != null && !channel.canTalk(member)) {
                bot.jda.openPrivateChannelById(reminder.memberId)
                    .flatMap { it.sendMessage(reminder.formattedMessage) }
                    .await()
            } else {
                channel.sendMessage(reminder.formattedMessage).await()
            }

            remindersToDelete.add(reminder.reminderId)
        }

        if (remindersToDelete.isNotEmpty()) {
            bot.mongoCollection<ReminderDTO>().deleteMany(ReminderDTO::reminderId `in` remindersToDelete)
        }
    }
}
