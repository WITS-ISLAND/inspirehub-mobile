package io.github.witsisland.inspirehub.data.mapper

import io.github.witsisland.inspirehub.data.dto.NodeDto
import io.github.witsisland.inspirehub.data.dto.ParentNodeDto
import io.github.witsisland.inspirehub.data.dto.ReactionSummaryDto
import io.github.witsisland.inspirehub.data.dto.ReactionsDto
import io.github.witsisland.inspirehub.domain.model.Node
import io.github.witsisland.inspirehub.domain.model.NodeType
import io.github.witsisland.inspirehub.domain.model.ParentNode
import io.github.witsisland.inspirehub.domain.model.ReactionSummary
import io.github.witsisland.inspirehub.domain.model.Reactions

/**
 * [NodeDto] から [Node] ドメインモデルへの変換
 *
 * 変換時の注意点:
 * - [NodeDto.type]: 小文字文字列 → [NodeType] enum。未知の値は [NodeType.IDEA] にフォールバック
 * - [NodeDto.content]: null → 空文字
 * - [NodeDto.authorName]: null → 空文字
 * - [NodeDto.tags]: [TagDto] のリストから名前のみ抽出して [Node.tagIds] にマッピング
 *   （フィールド名は "tagIds" だが実際にはタグ名のリスト）
 */
fun NodeDto.toDomain(): Node {
    return Node(
        id = id,
        type = when (type.lowercase()) {
            "issue" -> NodeType.ISSUE
            "idea" -> NodeType.IDEA
            "project" -> NodeType.PROJECT
            else -> NodeType.IDEA
        },
        title = title,
        content = content ?: "",
        authorId = authorId,
        authorName = authorName ?: "",
        authorPicture = authorPicture,
        parentNode = parentNode?.toDomain(),
        tagIds = tags.map { it.name },
        reactions = reactions.toDomain(),
        commentCount = commentCount,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

/**
 * [ParentNodeDto] から [ParentNode] ドメインモデルへの変換
 *
 * [NodeDto.toDomain] と同じ [NodeType] 変換ロジックを使用。
 */
fun ParentNodeDto.toDomain(): ParentNode {
    return ParentNode(
        id = id,
        type = when (type.lowercase()) {
            "issue" -> NodeType.ISSUE
            "idea" -> NodeType.IDEA
            "project" -> NodeType.PROJECT
            else -> NodeType.IDEA
        },
        title = title,
        content = content
    )
}

/**
 * [ReactionsDto] から [Reactions] ドメインモデルへの変換
 */
fun ReactionsDto.toDomain(): Reactions {
    return Reactions(
        like = like.toDomain(),
        interested = interested.toDomain(),
        wantToTry = wantToTry.toDomain()
    )
}

/**
 * [ReactionSummaryDto] から [ReactionSummary] ドメインモデルへの変換
 */
fun ReactionSummaryDto.toDomain(): ReactionSummary {
    return ReactionSummary(
        count = count,
        isReacted = isReacted
    )
}
