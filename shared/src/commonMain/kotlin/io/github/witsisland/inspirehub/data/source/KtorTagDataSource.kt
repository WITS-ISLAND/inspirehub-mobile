package io.github.witsisland.inspirehub.data.source

import io.github.witsisland.inspirehub.data.dto.NodeDto
import io.github.witsisland.inspirehub.data.dto.NodesResponseDto
import io.github.witsisland.inspirehub.data.dto.TagDto
import io.github.witsisland.inspirehub.data.dto.TagSuggestionsResponseDto
import io.github.witsisland.inspirehub.data.dto.TagsResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlin.native.HiddenFromObjC

/**
 * Ktor Client を使用した TagDataSource 実装
 *
 * API: /tags
 */
@HiddenFromObjC
class KtorTagDataSource(
    private val httpClient: HttpClient
) : TagDataSource {

    /**
     * GET /tags/popular
     * Response: { "tags": [TagDto], "total": number }
     */
    override suspend fun getPopularTags(limit: Int): List<TagDto> {
        val response: TagsResponseDto = httpClient.get("/tags/popular") {
            parameter("limit", limit)
        }.body()
        return response.tags
    }

    /**
     * GET /tags/suggest?q={query}&limit={limit}
     * Response: { "suggestions": [{ "id": string, "name": string }] }
     */
    override suspend fun suggestTags(query: String, limit: Int): List<TagDto> {
        val response: TagSuggestionsResponseDto = httpClient.get("/tags/suggest") {
            parameter("q", query)
            parameter("limit", limit)
        }.body()
        return response.suggestions.map { suggestion ->
            TagDto(
                id = suggestion.id,
                name = suggestion.name,
                usageCount = null,
                createdAt = ""
            )
        }
    }

    /**
     * GET /tags/{name}/nodes
     * Response: { "nodes": [NodeDto], "total": number }
     */
    override suspend fun getNodesByTagName(tagName: String, limit: Int, offset: Int): List<NodeDto> {
        val response: NodesResponseDto = httpClient.get("/tags/$tagName/nodes") {
            parameter("limit", limit)
            parameter("offset", offset)
        }.body()
        return response.nodes
    }
}
