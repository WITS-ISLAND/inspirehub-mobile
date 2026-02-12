package io.github.witsisland.inspirehub.data.mapper

import io.github.witsisland.inspirehub.data.dto.CommentDto
import io.github.witsisland.inspirehub.domain.model.Comment

/**
 * [CommentDto] から [Comment] ドメインモデルへの変換
 *
 * 変換時の注意点:
 * - [CommentDto.authorName]: null → 空文字
 * - [CommentDto.mentions]: [MentionDto] のリストから表示名のみ抽出
 *   （id, pictureの情報は失われる。Phase 2でメンション表示を強化する際に要改修）
 * - [CommentDto.replies]: 再帰的に変換
 * - [CommentDto.updatedAt]: ドメインモデルでは保持されない（Comment に updatedAt フィールドなし）
 */
fun CommentDto.toDomain(): Comment {
    return Comment(
        id = id,
        nodeId = nodeId,
        parentId = parentId,
        authorId = authorId,
        authorName = authorName ?: "",
        authorPicture = authorPicture,
        content = content,
        mentions = mentions.map { it.name },
        replies = replies.map { it.toDomain() },
        createdAt = createdAt
    )
}
