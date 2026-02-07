package io.github.witsisland.inspirehub.data.mapper

import io.github.witsisland.inspirehub.data.dto.UserDto
import io.github.witsisland.inspirehub.domain.model.User

fun UserDto.toDomain(): User {
    return User(
        id = id,
        handle = name,
        email = email,
        picture = picture,
        roleTag = null
    )
}
