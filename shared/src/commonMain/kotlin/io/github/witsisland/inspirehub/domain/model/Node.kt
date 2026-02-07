package io.github.witsisland.inspirehub.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Node(
    val id: String,
    val type: NodeType,
    val title: String,
    val content: String,
    val authorId: String,
    val authorName: String,
    val authorPicture: String? = null,
    val parentNode: ParentNode? = null,
    val tagIds: List<String> = emptyList(),
    val reactions: Reactions = Reactions(),
    val commentCount: Int = 0,
    val createdAt: String,
    val updatedAt: String? = null
)
