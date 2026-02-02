package io.github.witsisland.inspirehub.data.mapper

import io.github.witsisland.inspirehub.data.dto.NodeDto
import io.github.witsisland.inspirehub.domain.model.Node
import io.github.witsisland.inspirehub.domain.model.NodeType
import kotlinx.datetime.Instant

/**
 * NodeDto → Node への変換
 */
fun NodeDto.toDomain(): Node {
    return Node(
        id = id,
        type = when (type.lowercase()) {
            "issue" -> NodeType.ISSUE
            "idea" -> NodeType.IDEA
            else -> NodeType.IDEA // デフォルト値（"project" など未知の型の場合）
        },
        title = title,
        content = content,
        authorId = authorId,
        parentNodeId = null, // APIレスポンスにparentNodeIdがない場合はnull
        tagIds = tags.map { it.id },
        likeCount = likeCount,
        isLiked = isLiked,
        commentCount = commentCount,
        createdAt = Instant.parse(createdAt),
        updatedAt = Instant.parse(updatedAt)
    )
}
