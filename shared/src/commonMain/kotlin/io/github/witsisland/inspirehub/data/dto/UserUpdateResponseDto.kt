package io.github.witsisland.inspirehub.data.dto

import kotlinx.serialization.Serializable

/**
 * PATCH /users/me のレスポンスラッパー
 */
@Serializable
data class UserUpdateResponseDto(
    val user: UserDto
)
