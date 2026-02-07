package io.github.witsisland.inspirehub.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * POST /nodes/{nodeId}/comments リクエストボディ
 */
@Serializable
data class CreateCommentRequestDto(
    val content: String,
    @SerialName("parent_id")
    val parentId: String? = null
)

/**
 * PUT /comments/{id} リクエストボディ
 */
@Serializable
data class UpdateCommentRequestDto(
    val content: String
)
