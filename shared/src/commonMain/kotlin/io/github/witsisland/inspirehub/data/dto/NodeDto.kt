package io.github.witsisland.inspirehub.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NodeDto(
    val id: String,
    val title: String,
    val content: String? = null,
    val type: String,
    @SerialName("author_id")
    val authorId: String,
    @SerialName("author_name")
    val authorName: String = "",
    @SerialName("author_picture")
    val authorPicture: String? = null,
    @SerialName("parentNode")
    val parentNode: ParentNodeDto? = null,
    val tags: List<TagDto> = emptyList(),
    val reactions: ReactionsDto = ReactionsDto(),
    @SerialName("comment_count")
    val commentCount: Int = 0,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String? = null
)
