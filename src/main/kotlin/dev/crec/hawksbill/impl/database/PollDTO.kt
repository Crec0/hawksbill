package dev.crec.hawksbill.impl.database

import dev.crec.hawksbill.api.database.Entity
import kotlinx.serialization.Serializable

@Serializable
data class PollDTO(
    val vote_id: String,
    val channel_id: String,
    val question: String,
    val options: Map<String, String>,
    val votes: Map<String, String> = mapOf(),
) : Entity
