package dev.crec.hawksbill.impl.database

import dev.crec.hawksbill.api.database.Entity
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class WhiteListDTO(
    @SerialName("member_id")
    val memberId: String,
    @Contextual
    val uuid: UUID
) : Entity
