package io.github.witsisland.inspirehub.data.dto

import kotlinx.serialization.Serializable

/**
 * PATCH /users/me リクエストボディ
 */
@Serializable
data class UserUpdateRequestDto(
    val name: String
)
