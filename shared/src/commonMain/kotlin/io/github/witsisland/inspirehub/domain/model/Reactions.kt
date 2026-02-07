package io.github.witsisland.inspirehub.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Reactions(
    val like: ReactionSummary = ReactionSummary(),
    val interested: ReactionSummary = ReactionSummary(),
    val wantToTry: ReactionSummary = ReactionSummary()
)
