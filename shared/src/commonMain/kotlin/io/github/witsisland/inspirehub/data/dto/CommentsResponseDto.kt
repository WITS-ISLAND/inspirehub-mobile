package io.github.witsisland.inspirehub.data.dto

import kotlinx.serialization.Serializable

/**
 * GET /nodes/{nodeId}/comments のレスポンスラッパー
 */
@Serializable
data class CommentsResponseDto(
    val comments: List<CommentDto>,
    val total: Int
)
