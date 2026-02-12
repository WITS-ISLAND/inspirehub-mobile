package io.github.witsisland.inspirehub.data.mapper

import io.github.witsisland.inspirehub.data.dto.TagDto
import io.github.witsisland.inspirehub.domain.model.Tag

/**
 * [TagDto] から [Tag] ドメインモデルへの変換
 *
 * 変換時の注意点:
 * - [TagDto.usageCount]: null → 0（エンドポイントによってはusage_countが返されない）
 * - [TagDto.createdAt]: DTOでは空文字デフォルト、ドメインモデルではそのまま渡される
 */
fun TagDto.toDomain(): Tag {
    return Tag(
        id = id,
        name = name,
        usageCount = usageCount ?: 0,
        createdAt = createdAt
    )
}
