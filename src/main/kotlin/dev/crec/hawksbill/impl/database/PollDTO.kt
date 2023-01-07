package dev.crec.hawksbill.impl.database

import dev.crec.hawksbill.api.database.Entity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PollDTO(
    @SerialName("vote_id")
    val voteId: String,
    @SerialName("channel_id")
    val channelId: String,
    val question: String,
    val options: Map<String, String>,
    val votes: Map<String, String> = mapOf(),
) : Entity
