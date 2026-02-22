package io.github.witsisland.inspirehub.domain.repository

import io.github.witsisland.inspirehub.domain.model.ReactedUsersPage
import io.github.witsisland.inspirehub.domain.model.ReactionSummary
import io.github.witsisland.inspirehub.domain.model.ReactionType
import kotlin.native.HiddenFromObjC

@HiddenFromObjC
interface ReactionRepository {
    suspend fun toggleReaction(nodeId: String, type: ReactionType): Result<ReactionSummary>
    suspend fun getReactedUsers(nodeId: String, type: ReactionType, limit: Int = 30, cursor: String? = null): Result<ReactedUsersPage>
}
