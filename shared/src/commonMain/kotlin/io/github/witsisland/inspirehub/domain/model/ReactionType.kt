package io.github.witsisland.inspirehub.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class ReactionType {
    LIKE,
    INTERESTED,
    WANT_TO_TRY
}
