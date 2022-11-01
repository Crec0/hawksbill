package dev.crec.hawksbill.impl.database

import dev.crec.hawksbill.api.database.Entity
import kotlinx.serialization.Serializable

@Serializable
data class ReminderDTO(
    val reminder_id: String,
    val member_id: String,
    val channel_id: String,
    val message: String,
    val created: String,
    val expiry: String,
) : Entity
