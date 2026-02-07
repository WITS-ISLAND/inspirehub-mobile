package io.github.witsisland.inspirehub.data.dto

import kotlinx.serialization.Serializable

/**
 * GET /tags/suggest のレスポンスラッパー
 */
@Serializable
data class TagSuggestionDto(
    val id: String,
    val name: String
)

@Serializable
data class TagSuggestionsResponseDto(
    val suggestions: List<TagSuggestionDto>
)
