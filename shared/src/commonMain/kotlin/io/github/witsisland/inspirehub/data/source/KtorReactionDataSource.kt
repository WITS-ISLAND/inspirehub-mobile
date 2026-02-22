package io.github.witsisland.inspirehub.data.source

import io.github.witsisland.inspirehub.data.dto.ReactedUsersResponseDto
import io.github.witsisland.inspirehub.data.dto.ReactionSummaryDto
import io.github.witsisland.inspirehub.domain.model.ReactionType
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import kotlin.native.HiddenFromObjC

/**
 * Ktor Client を使用した ReactionDataSource 実装
 *
 * API: /nodes/{id}/like, /nodes/{id}/interested, /nodes/{id}/want-to-try
 * 全エンドポイントとも POST (bodyなし)
 * Response: { "count": number, "is_reacted": boolean }
 */
@HiddenFromObjC
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

    /**
     * GET /nodes/{nodeId}/reactions/{type}
     *
     * リアクション種別に応じたエンドポイントパス:
     * - [ReactionType.LIKE] → `/nodes/{nodeId}/reactions/like`
     * - [ReactionType.INTERESTED] → `/nodes/{nodeId}/reactions/interested`
     * - [ReactionType.WANT_TO_TRY] → `/nodes/{nodeId}/reactions/want-to-try`
     *
     * クエリパラメータ: `limit`（件数）, `cursor`（ページネーション用カーソル）
     */
    override suspend fun getReactionUsers(
        nodeId: String,
        type: ReactionType,
        limit: Int,
        cursor: String?
    ): ReactedUsersResponseDto {
        val path = when (type) {
            ReactionType.LIKE -> "/nodes/$nodeId/reactions/like"
            ReactionType.INTERESTED -> "/nodes/$nodeId/reactions/interested"
            ReactionType.WANT_TO_TRY -> "/nodes/$nodeId/reactions/want-to-try"
        }
        return httpClient.get(path) {
            parameter("limit", limit)
            cursor?.let { parameter("cursor", it) }
        }.body()
    }
}
