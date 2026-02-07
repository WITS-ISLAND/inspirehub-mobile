package io.github.witsisland.inspirehub.data.dto

import kotlinx.serialization.Serializable

/**
 * GET /tags/popular のレスポンスラッパー
 */
@Serializable
data class TagsResponseDto(
    val tags: List<TagDto>,
    val total: Int
)
