package io.github.witsisland.inspirehub.data.mapper

import io.github.witsisland.inspirehub.data.dto.CommentDto
import io.github.witsisland.inspirehub.domain.model.Comment

fun CommentDto.toDomain(): Comment {
    return Comment(
        id = id,
        nodeId = nodeId,
        parentId = parentId,
        authorId = authorId,
        authorName = authorName,
        authorPicture = authorPicture,
        content = content,
        mentions = mentions,
        replies = replies.map { it.toDomain() },
        createdAt = createdAt
    )
}
