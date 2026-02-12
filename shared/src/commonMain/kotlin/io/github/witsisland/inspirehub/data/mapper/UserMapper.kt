package io.github.witsisland.inspirehub.data.mapper

import io.github.witsisland.inspirehub.data.dto.UserDto
import io.github.witsisland.inspirehub.domain.model.User

/**
 * [UserDto] から [User] ドメインモデルへの変換
 *
 * 変換時の注意点:
 * - [UserDto.name] → [User.handle]: APIの "name" をドメインモデルでは "handle" として扱う
 * - [User.roleTag]: 現在のAPI仕様にはないため常にnull。将来の拡張用
 */
fun UserDto.toDomain(): User {
    return User(
        id = id,
        handle = name,
        email = email,
        picture = picture,
        roleTag = null
    )
}
