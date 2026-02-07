package io.github.witsisland.inspirehub.domain.repository

import io.github.witsisland.inspirehub.domain.model.ReactionSummary
import io.github.witsisland.inspirehub.domain.model.ReactionType

interface ReactionRepository {
    suspend fun toggleReaction(nodeId: String, type: ReactionType): Result<ReactionSummary>
}
