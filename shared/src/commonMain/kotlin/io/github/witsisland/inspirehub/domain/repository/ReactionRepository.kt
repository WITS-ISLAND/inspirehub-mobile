package io.github.witsisland.inspirehub.domain.repository

import io.github.witsisland.inspirehub.domain.model.ReactionSummary
import io.github.witsisland.inspirehub.domain.model.ReactionType
import kotlin.native.HiddenFromObjC

@HiddenFromObjC
interface ReactionRepository {
    suspend fun toggleReaction(nodeId: String, type: ReactionType): Result<ReactionSummary>
}
