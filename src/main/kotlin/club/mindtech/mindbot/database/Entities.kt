package club.mindtech.mindbot.database

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

interface Entity

@Serializable
data class MemberTimeZone(
    val member_id: String,
    @Contextual
    val time_zone: TimeZone
) : Entity

@Serializable
data class Poll(
    val vote_id: String,
    val question: String,
    @Contextual
    val options: Map<String, String>,
    @Contextual
    val votes: Map<String, String> = mapOf(),
) : Entity

@Serializable
data class RemindMe(
    val member_id: String,
    val message: String,
    val time: Long
) : Entity

@Serializable
data class WhiteList(
    val member_id: String,
    @Contextual
    val uuid: UUID
) : Entity
