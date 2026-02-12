package io.github.witsisland.inspirehub.data.dto

import kotlinx.serialization.Serializable

/**
 * `PATCH /users/me` のレスポンスDTO
 *
 * ユーザー情報更新成功時に、更新後のユーザー情報を返す。Bearer認証が必要。
 * APIレスポンス形式: `{ "user": { ... } }`
 *
 * @property user 更新後のユーザー情報
 * @see UserDto ユーザー情報のデータ構造
 */
@Serializable
data class UserUpdateResponseDto(
    val user: UserDto
)
