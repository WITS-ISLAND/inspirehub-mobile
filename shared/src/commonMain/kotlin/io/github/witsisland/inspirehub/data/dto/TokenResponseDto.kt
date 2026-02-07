package io.github.witsisland.inspirehub.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * API Token レスポンス（認証時）
 */
@Serializable
data class TokenResponseDto(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("refresh_token")
    val refreshToken: String,
    @SerialName("expires_in")
    val expiresIn: Int,
    val user: UserDto? = null
)
