package club.mindtech.mindbot.database

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class MemberTimeZone(
    val member_id: String,
    @Contextual
    val time_zone: TimeZone
)

@Serializable
data class Poll(
    val vote_id: String,
    val question: String,
    @Contextual
    val options: Map<String, String>,
    @Contextual
    val votes: Map<String, String> = mapOf(),
)

@Serializable
data class RemindMe(
    val member_id: String,
    val message: String,
    val time: Long
)

@Serializable
data class WhiteList(
    val member_id: String,
    @Contextual
    val uuid: UUID
)
