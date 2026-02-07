package io.github.witsisland.inspirehub.data.dto

import kotlinx.serialization.Serializable

/**
 * GET /auth/me のレスポンスラッパー
 * API: { "user": UserDto }
 */
@Serializable
data class AuthMeResponseDto(
    val user: UserDto
)
