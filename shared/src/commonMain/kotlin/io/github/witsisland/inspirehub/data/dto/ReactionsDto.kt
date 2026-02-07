package io.github.witsisland.inspirehub.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReactionsDto(
    val like: ReactionSummaryDto = ReactionSummaryDto(),
    val interested: ReactionSummaryDto = ReactionSummaryDto(),
    @SerialName("want_to_try")
    val wantToTry: ReactionSummaryDto = ReactionSummaryDto()
)
