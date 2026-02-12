package io.github.witsisland.inspirehub.data.dto

import kotlinx.serialization.Serializable

/**
 * `PATCH /users/me` のリクエストボディDTO
 *
 * ログインユーザーの表示名を更新する。Bearer認証が必要。
 *
 * @property name 更新後の表示名（1文字以上必須）
 */
@Serializable
data class UserUpdateRequestDto(
    val name: String
)
