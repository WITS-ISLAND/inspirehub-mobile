package io.github.witsisland.inspirehub.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReactionSummaryDto(
    val count: Int = 0,
    @SerialName("is_reacted")
    val isReacted: Boolean = false
)
