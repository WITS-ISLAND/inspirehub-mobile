package io.github.witsisland.inspirehub.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * ノード（課題・アイデア・プロジェクト）のDTO
 *
 * `GET /nodes` のレスポンス配列要素、および `GET /nodes/{id}` のレスポンスとして使用。
 * APIのJSON key名はsnake_caseで、[SerialName] アノテーションでマッピングしている。
 *
 * @property id ノードの一意識別子
 * @property title ノードのタイトル
 * @property content ノードの本文（APIからnullが返る場合がある）
 * @property type ノード種別。APIからは小文字文字列（"idea", "issue", "project"）で返却
 * @property authorId 投稿者のユーザーID
 * @property authorName 投稿者の表示名（APIからnullが返る場合がある）
 * @property authorPicture 投稿者のプロフィール画像URL
 * @property parentNode 派生元ノード。派生アイデアの場合のみ存在
 * @property tags ノードに付与されたタグ一覧
 * @property reactions リアクション集計（いいね・共感・作ってみたい）
 * @property commentCount コメント数
 * @property createdAt 作成日時（ISO 8601形式）
 * @property updatedAt 更新日時（ISO 8601形式、未更新の場合null）
 * @see Node ドメインモデルへの変換先
 * @see io.github.witsisland.inspirehub.data.mapper.NodeMapper 変換ロジック
 */
@Serializable
data class NodeDto(
    val id: String,
    val title: String,
    val content: String? = null,
    val type: String,
    @SerialName("author_id")
    val authorId: String,
    @SerialName("author_name")
    val authorName: String? = null,
    @SerialName("author_picture")
    val authorPicture: String? = null,
    @SerialName("parent_node")
    val parentNode: ParentNodeDto? = null,
    val tags: List<TagDto> = emptyList(),
    val reactions: ReactionsDto = ReactionsDto(),
    @SerialName("comment_count")
    val commentCount: Int = 0,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String? = null
)
