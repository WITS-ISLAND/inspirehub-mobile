package io.github.witsisland.inspirehub.data.source

import io.github.witsisland.inspirehub.data.dto.ReactionSummaryDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post

/**
 * Ktor Client を使用した ReactionDataSource 実装
 */
class KtorReactionDataSource(
    private val httpClient: HttpClient
) : ReactionDataSource {

    override suspend fun toggleLike(nodeId: String): ReactionSummaryDto {
        return httpClient.post("/nodes/$nodeId/like").body()
    }

    override suspend fun toggleInterested(nodeId: String): ReactionSummaryDto {
        return httpClient.post("/nodes/$nodeId/interested").body()
    }

    override suspend fun toggleWantToTry(nodeId: String): ReactionSummaryDto {
        return httpClient.post("/nodes/$nodeId/want-to-try").body()
    }
}
