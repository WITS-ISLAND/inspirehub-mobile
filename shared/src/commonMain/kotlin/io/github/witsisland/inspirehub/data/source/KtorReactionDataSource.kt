package io.github.witsisland.inspirehub.data.source

import io.github.witsisland.inspirehub.data.dto.ReactionSummaryDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post

/**
 * Ktor Client を使用した ReactionDataSource 実装
 *
 * API: /nodes/{id}/like, /nodes/{id}/interested, /nodes/{id}/want-to-try
 * 全エンドポイントとも POST (bodyなし)
 * Response: { "count": number, "is_reacted": boolean }
 */
class KtorReactionDataSource(
    private val httpClient: HttpClient
) : ReactionDataSource {

    /**
     * POST /nodes/{nodeId}/like
     * Response: { "count": number, "is_reacted": boolean }
     */
    override suspend fun toggleLike(nodeId: String): ReactionSummaryDto {
        return httpClient.post("/nodes/$nodeId/like").body()
    }

    /**
     * POST /nodes/{nodeId}/interested
     * Response: { "count": number, "is_reacted": boolean }
     */
    override suspend fun toggleInterested(nodeId: String): ReactionSummaryDto {
        return httpClient.post("/nodes/$nodeId/interested").body()
    }

    /**
     * POST /nodes/{nodeId}/want-to-try
     * Response: { "count": number, "is_reacted": boolean }
     */
    override suspend fun toggleWantToTry(nodeId: String): ReactionSummaryDto {
        return httpClient.post("/nodes/$nodeId/want-to-try").body()
    }
}
