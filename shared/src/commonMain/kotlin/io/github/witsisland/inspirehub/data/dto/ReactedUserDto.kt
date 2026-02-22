package io.github.witsisland.inspirehub.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * リアクションを行ったユーザーのDTO
 *
 * `GET /nodes/{id}/reactions/{type}` のレスポンス配列要素として使用。
 *
 * @property userId ユーザーID
 * @property userName ユーザー名（未設定の場合は null）
 * @property userPicture プロフィール画像URL（未設定の場合は null）
 * @property reactedAt リアクション日時（ISO 8601形式）
 */
@Serializable
data class ReactedUserDto(
    @SerialName("user_id") val userId: String,
    @SerialName("user_name") val userName: String? = null,
    @SerialName("user_picture") val userPicture: String? = null,
    @SerialName("reacted_at") val reactedAt: String
)

/**
 * リアクションユーザー一覧のページネーション付きレスポンスDTO
 *
 * `GET /nodes/{id}/reactions/{type}` のレスポンス全体として使用。
 *
 * @property data リアクションしたユーザーのリスト
 * @property nextCursor 次ページ取得用カーソル（次ページが存在しない場合は null）
 * @property hasMore 次ページが存在するかどうか
 * @property total リアクションユーザーの総数
 */
@Serializable
data class ReactedUsersResponseDto(
    val data: List<ReactedUserDto>,
    @SerialName("next_cursor") val nextCursor: String? = null,
    @SerialName("has_more") val hasMore: Boolean,
    val total: Int
)
