package io.github.witsisland.inspirehub.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Comment(
    val id: String,
    val nodeId: String,
    val parentId: String? = null,
    val authorId: String,
    val authorName: String,
    val authorPicture: String? = null,
    val content: String,
    val mentions: List<String> = emptyList(),
    val replies: List<Comment> = emptyList(),
    val createdAt: String
)
