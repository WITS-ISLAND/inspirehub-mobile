package io.github.witsisland.inspirehub.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ReactionSummary(
    val count: Int = 0,
    val isReacted: Boolean = false
)
