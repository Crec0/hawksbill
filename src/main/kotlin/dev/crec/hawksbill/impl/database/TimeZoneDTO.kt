package dev.crec.hawksbill.impl.database

import dev.crec.hawksbill.api.database.Entity
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.TimeZone

@Serializable
data class TimeZoneDTO(
    val member_id: String,
    @Contextual
    val time_zone: TimeZone
) : Entity

