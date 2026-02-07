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
        authorName = authorName,
        authorPicture = authorPicture,
        parentNode = parentNode?.toDomain(),
        tagIds = tags.map { it.name },
        reactions = reactions.toDomain(),
        commentCount = commentCount,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

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

fun ReactionsDto.toDomain(): Reactions {
    return Reactions(
        like = like.toDomain(),
        interested = interested.toDomain(),
        wantToTry = wantToTry.toDomain()
    )
}

fun ReactionSummaryDto.toDomain(): ReactionSummary {
    return ReactionSummary(
        count = count,
        isReacted = isReacted
    )
}
