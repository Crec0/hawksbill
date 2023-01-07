package dev.crec.hawksbill.impl.database

import dev.crec.hawksbill.api.database.Entity
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.TimeZone

@Serializable
data class TimeZoneDTO(
    @SerialName("member_id")
    val memberId: String,
    @Contextual
    @SerialName("timezone")
    val timeZone: TimeZone
) : Entity

