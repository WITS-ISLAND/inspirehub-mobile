package io.github.witsisland.inspirehub.data.source

import io.github.witsisland.inspirehub.data.dto.ReactionSummaryDto

interface ReactionDataSource {
    suspend fun toggleLike(nodeId: String): ReactionSummaryDto
    suspend fun toggleInterested(nodeId: String): ReactionSummaryDto
    suspend fun toggleWantToTry(nodeId: String): ReactionSummaryDto
}
