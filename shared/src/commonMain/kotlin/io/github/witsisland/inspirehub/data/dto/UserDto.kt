package io.github.witsisland.inspirehub.data.dto

import kotlinx.serialization.Serializable

/**
 * ユーザー情報のDTO
 *
 * 認証レスポンス、ノード・コメントの著者情報など、API全体で共通して使用される。
 *
 * @property id ユーザーの一意識別子
 * @property name ユーザーの表示名（ドメインモデルでは [User.handle] にマッピング）
 * @property email ユーザーのメールアドレス
 * @property picture プロフィール画像URL（設定されていない場合null）
 * @see User ドメインモデルへの変換先
 * @see io.github.witsisland.inspirehub.data.mapper.UserMapper 変換ロジック
 */
@Serializable
data class UserDto(
    val id: String,
    val name: String,
    val email: String,
    val picture: String? = null
)
