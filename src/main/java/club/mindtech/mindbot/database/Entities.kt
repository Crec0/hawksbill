package club.mindtech.mindbot.database

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
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
    @Contextual
    val votes: Map<String, String> = mutableMapOf(),
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
