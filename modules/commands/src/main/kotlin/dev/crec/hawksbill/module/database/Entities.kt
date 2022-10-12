package dev.crec.hawksbill.module.database

import dev.crec.hawksbill.api.database.Entity
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.TimeZone
import java.util.UUID

@Serializable
data class MemberTimeZone(
    val member_id: String,
    @Contextual
    val time_zone: TimeZone
) : Entity

@Serializable
data class Poll(
    val vote_id: String,
    val channel_id: String,
    val question: String,
    val options: Map<String, String>,
    val votes: Map<String, String> = mapOf(),
) : Entity

@Serializable
data class Reminder(
    val reminder_id: String,
    val member_id: String,
    val channel_id: String,
    val message: String,
    val created: String,
    val expiry: String,
) : Entity

@Serializable
data class WhiteList(
    val member_id: String,
    @Contextual
    val uuid: UUID
) : Entity
