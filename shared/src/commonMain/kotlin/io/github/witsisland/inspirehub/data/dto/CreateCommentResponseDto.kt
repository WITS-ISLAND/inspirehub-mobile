package io.github.witsisland.inspirehub.data.dto

import kotlinx.serialization.Serializable

/**
 * POST /nodes/{nodeId}/comments のレスポンス (201 Created)
 * API: { "id": string, "message": string }
 */
@Serializable
data class CreateCommentResponseDto(
    val id: String,
    val message: String
)
