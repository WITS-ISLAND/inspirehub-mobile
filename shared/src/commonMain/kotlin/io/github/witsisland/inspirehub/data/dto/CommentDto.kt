package io.github.witsisland.inspirehub.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CommentDto(
    val id: String,
    val content: String,
    @SerialName("author_id")
    val authorId: String,
    @SerialName("author_name")
    val authorName: String = "",
    @SerialName("author_picture")
    val authorPicture: String? = null,
    @SerialName("node_id")
    val nodeId: String,
    @SerialName("parent_id")
    val parentId: String? = null,
    val mentions: List<String> = emptyList(),
    val replies: List<CommentDto> = emptyList(),
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String? = null
)
