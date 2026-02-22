package io.github.witsisland.inspirehub.domain.repository

import io.github.witsisland.inspirehub.domain.model.ReactedUsersPage
import io.github.witsisland.inspirehub.domain.model.ReactionSummary
import io.github.witsisland.inspirehub.domain.model.ReactionType
import kotlin.native.HiddenFromObjC

@HiddenFromObjC
class FakeReactionRepository : ReactionRepository {

    var toggleReactionResult: Result<ReactionSummary>? = null
    var getReactedUsersResult: Result<ReactedUsersPage>? = null

    var shouldReturnError: Boolean = false
    var errorMessage: String = "Test error"

    var toggleReactionCallCount = 0
    var lastToggleReactionNodeId: String? = null
    var lastToggleReactionType: ReactionType? = null

    var getReactedUsersCallCount = 0
    var lastGetReactedUsersNodeId: String? = null
    var lastGetReactedUsersType: ReactionType? = null
    var lastGetReactedUsersLimit: Int? = null
    var lastGetReactedUsersCursor: String? = null

    override suspend fun toggleReaction(nodeId: String, type: ReactionType): Result<ReactionSummary> {
        toggleReactionCallCount++
        lastToggleReactionNodeId = nodeId
        lastToggleReactionType = type

        if (shouldReturnError) return Result.failure(Exception(errorMessage))
        return toggleReactionResult ?: error("toggleReactionResult not set")
    }

    override suspend fun getReactedUsers(
        nodeId: String,
        type: ReactionType,
        limit: Int,
        cursor: String?
    ): Result<ReactedUsersPage> {
        getReactedUsersCallCount++
        lastGetReactedUsersNodeId = nodeId
        lastGetReactedUsersType = type
        lastGetReactedUsersLimit = limit
        lastGetReactedUsersCursor = cursor

        if (shouldReturnError) return Result.failure(Exception(errorMessage))
        return getReactedUsersResult ?: error("getReactedUsersResult not set")
    }

    fun reset() {
        toggleReactionResult = null
        getReactedUsersResult = null
        shouldReturnError = false
        errorMessage = "Test error"
        toggleReactionCallCount = 0
        lastToggleReactionNodeId = null
        lastToggleReactionType = null
        getReactedUsersCallCount = 0
        lastGetReactedUsersNodeId = null
        lastGetReactedUsersType = null
        lastGetReactedUsersLimit = null
        lastGetReactedUsersCursor = null
    }
}
