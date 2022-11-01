package dev.crec.hawksbill.impl.database

import dev.crec.hawksbill.api.database.Entity
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class WhiteListDTO(
    val member_id: String,
    @Contextual
    val uuid: UUID
) : Entity
