package dev.crec.hawksbill.impl.database

import dev.crec.hawksbill.api.database.Entity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.dv8tion.jda.api.entities.User
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.Delayed
import java.util.concurrent.TimeUnit

@Serializable
data class ReminderDTO(
    @SerialName("reminder_id")
    val reminderId: String,
    @SerialName("member_id")
    val memberId: String,
    @SerialName("channel_id")
    val channelId: String,
    val message: String,
    val created: Long,
    val expiry: Long,
) : Entity, Delayed {

    val formattedMessage =
        """
        Reminder for ${User.fromId(this.memberId).asMention}
        > ${this.message}
        *Created: <t:${this.created}:R>*
        """.trimIndent()

    override fun getDelay(unit: TimeUnit): Long {
        return unit.convert(Duration.of(this.expiry - Instant.now().epochSecond, ChronoUnit.SECONDS))
    }

    override fun compareTo(other: Delayed?): Int {
        if (other == null || other !is ReminderDTO) {
            return -1
        }
        // We compare the expiry because compareTo is being used by the DelayQueue along with getDelay according to docs
        return this.expiry.compareTo(other.expiry)
    }
}
