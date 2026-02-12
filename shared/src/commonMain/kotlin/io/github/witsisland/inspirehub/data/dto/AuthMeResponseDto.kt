package io.github.witsisland.inspirehub.data.dto

import kotlinx.serialization.Serializable

/**
 * `GET /auth/me` のレスポンスDTO
 *
 * 現在認証中のユーザー情報を返す。Bearer認証が必要。
 * APIレスポンス形式: `{ "user": { ... } }`
 *
 * @property user 認証中のユーザー情報
 * @see UserDto ユーザー情報のデータ構造
 */
@Serializable
data class AuthMeResponseDto(
    val user: UserDto
)
