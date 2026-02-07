package io.github.witsisland.inspirehub.data.mapper

import io.github.witsisland.inspirehub.data.dto.TagDto
import io.github.witsisland.inspirehub.domain.model.Tag

fun TagDto.toDomain(): Tag {
    return Tag(
        id = id,
        name = name,
        usageCount = usageCount ?: 0,
        createdAt = createdAt
    )
}
