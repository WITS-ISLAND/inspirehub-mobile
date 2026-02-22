package io.github.witsisland.inspirehub.data.source

import io.github.witsisland.inspirehub.data.dto.ReactedUsersResponseDto
import io.github.witsisland.inspirehub.data.dto.ReactionSummaryDto
import io.github.witsisland.inspirehub.domain.model.ReactionType
import kotlin.native.HiddenFromObjC

@HiddenFromObjC
interface ReactionDataSource {
    suspend fun toggleLike(nodeId: String): ReactionSummaryDto
    suspend fun toggleInterested(nodeId: String): ReactionSummaryDto
    suspend fun toggleWantToTry(nodeId: String): ReactionSummaryDto
    suspend fun getReactionUsers(nodeId: String, type: ReactionType, limit: Int = 30, cursor: String? = null): ReactedUsersResponseDto
}
