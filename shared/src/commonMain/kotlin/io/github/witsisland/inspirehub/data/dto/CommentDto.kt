package io.github.witsisland.inspirehub.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * コメントのDTO
 *
 * `GET /nodes/{nodeId}/comments` のレスポンス配列要素として使用。
 * [replies] フィールドにより再帰的なスレッド構造を表現する。
 *
 * @property id コメントの一意識別子
 * @property content コメント本文
 * @property authorId 投稿者のユーザーID
 * @property authorName 投稿者の表示名
 * @property authorPicture 投稿者のプロフィール画像URL
 * @property nodeId コメントが属するノードのID
 * @property parentId 返信先コメントのID（トップレベルコメントの場合null）
 * @property mentions メンションされたユーザー一覧
 * @property replies このコメントへの返信一覧（再帰構造）
 * @property createdAt 作成日時（ISO 8601形式）
 * @property updatedAt 更新日時（ISO 8601形式、未更新の場合null）
 * @see Comment ドメインモデルへの変換先
 * @see CommentDto.toDomain 変換ロジック
 * @see MentionDto メンションユーザーのデータ構造
 */
@Serializable
data class CommentDto(
    val id: String,
    val content: String,
    @SerialName("author_id")
    val authorId: String,
    @SerialName("author_name")
    val authorName: String? = null,
    @SerialName("author_picture")
    val authorPicture: String? = null,
    @SerialName("node_id")
    val nodeId: String,
    @SerialName("parent_id")
    val parentId: String? = null,
    val mentions: List<MentionDto> = emptyList(),
    val replies: List<CommentDto> = emptyList(),
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String? = null
)
