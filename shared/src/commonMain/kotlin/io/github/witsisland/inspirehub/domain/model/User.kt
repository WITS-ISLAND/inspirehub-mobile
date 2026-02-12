package io.github.witsisland.inspirehub.domain.model

import kotlinx.serialization.Serializable

/**
 * ユーザーのドメインモデル
 *
 * InspireHubに登録されたユーザーを表す。Google OAuth認証で取得した情報を基に構成される。
 *
 * @property id ユーザーの一意識別子
 * @property handle ユーザーの表示名。APIの `name` フィールドに対応する
 * @property email ユーザーのメールアドレス
 * @property picture プロフィール画像URL（Google アカウントのアイコン等）
 * @property roleTag ユーザーの役割タグ（例: "バックエンドエンジニア"）。
 *                   現在のAPI仕様には存在せず、将来の拡張用フィールド（常にnull）
 */
@Serializable
data class User(
    val id: String,
    val handle: String,
    val email: String = "",
    val picture: String? = null,
    val roleTag: String? = null
)
