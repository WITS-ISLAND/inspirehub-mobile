package io.github.witsisland.inspirehub.data.source

import io.github.witsisland.inspirehub.data.dto.TagDto
import io.github.witsisland.inspirehub.data.dto.TagSuggestionsResponseDto
import io.github.witsisland.inspirehub.data.dto.TagsResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

/**
 * Ktor Client を使用した TagDataSource 実装
 */
class KtorTagDataSource(
    private val httpClient: HttpClient
) : TagDataSource {

    override suspend fun getPopularTags(limit: Int): List<TagDto> {
        val response: TagsResponseDto = httpClient.get("/tags/popular").body()
        return response.tags.take(limit)
    }

    override suspend fun suggestTags(query: String, limit: Int): List<TagDto> {
        val response: TagSuggestionsResponseDto = httpClient.get("/tags/suggest") {
            parameter("q", query)
            parameter("limit", limit)
        }.body()
        // TagSuggestionDto → TagDto に変換（suggest はusageCountとcreatedAtが無い）
        return response.suggestions.map { suggestion ->
            TagDto(
                id = suggestion.id,
                name = suggestion.name,
                usageCount = null,
                createdAt = ""
            )
        }
    }
}
