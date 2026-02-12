package io.github.witsisland.inspirehub.domain.repository

import io.github.witsisland.inspirehub.domain.model.ReactionSummary
import io.github.witsisland.inspirehub.domain.model.ReactionType
import kotlin.native.HiddenFromObjC

@HiddenFromObjC
class FakeReactionRepository : ReactionRepository {

    var toggleReactionResult: Result<ReactionSummary>? = null

    var shouldReturnError: Boolean = false
    var errorMessage: String = "Test error"

    var toggleReactionCallCount = 0
    var lastToggleReactionNodeId: String? = null
    var lastToggleReactionType: ReactionType? = null

    override suspend fun toggleReaction(nodeId: String, type: ReactionType): Result<ReactionSummary> {
        toggleReactionCallCount++
        lastToggleReactionNodeId = nodeId
        lastToggleReactionType = type

        if (shouldReturnError) return Result.failure(Exception(errorMessage))
        return toggleReactionResult ?: error("toggleReactionResult not set")
    }

    fun reset() {
        toggleReactionResult = null
        shouldReturnError = false
        errorMessage = "Test error"
        toggleReactionCallCount = 0
        lastToggleReactionNodeId = null
        lastToggleReactionType = null
    }
}
