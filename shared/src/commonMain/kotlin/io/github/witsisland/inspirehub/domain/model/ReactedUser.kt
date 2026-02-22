package io.github.witsisland.inspirehub.domain.model

/**
 * リアクションを行ったユーザーのドメインモデル
 *
 * @property userId ユーザーID
 * @property userName ユーザー名（未設定の場合は null）
 * @property userPicture プロフィール画像URL（未設定の場合は null）
 * @property reactedAt リアクション日時（ISO 8601形式）
 */
data class ReactedUser(
    val userId: String,
    val userName: String?,
    val userPicture: String?,
    val reactedAt: String
)

/**
 * リアクションユーザー一覧のページネーション付きドメインモデル
 *
 * @property users リアクションしたユーザーのリスト
 * @property nextCursor 次ページ取得用カーソル（次ページが存在しない場合は null）
 * @property hasMore 次ページが存在するかどうか
 * @property total リアクションユーザーの総数
 */
data class ReactedUsersPage(
    val users: List<ReactedUser>,
    val nextCursor: String?,
    val hasMore: Boolean,
    val total: Int
)
