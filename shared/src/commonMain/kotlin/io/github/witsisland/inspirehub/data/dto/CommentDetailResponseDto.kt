package io.github.witsisland.inspirehub.data.dto

import kotlinx.serialization.Serializable

/**
 * GET /comments/{id} のレスポンスラッパー
 * API: { "comment": CommentDto }
 */
@Serializable
data class CommentDetailResponseDto(
    val comment: CommentDto
)
