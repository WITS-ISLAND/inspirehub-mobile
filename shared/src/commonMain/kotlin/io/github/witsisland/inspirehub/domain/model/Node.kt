package io.github.witsisland.inspirehub.domain.model

import kotlinx.serialization.Serializable

/**
 * ノード（課題・アイデア・プロジェクト）のドメインモデル
 *
 * InspireHubにおける投稿の基本単位。ユーザーが投稿する「課題」「アイデア」「プロジェクト」を
 * 統一的に扱う。派生アイデアは [parentNode] を通じて元の投稿との関連を持つ。
 *
 * @property id ノードの一意識別子
 * @property type ノードの種別（課題・アイデア・プロジェクト）
 * @property title ノードのタイトル
 * @property content ノードの本文（DTOではnullableだが、変換時に空文字にフォールバック）
 * @property authorId 投稿者のユーザーID
 * @property authorName 投稿者の表示名（DTOではnullableだが、変換時に空文字にフォールバック）
 * @property authorPicture 投稿者のプロフィール画像URL
 * @property parentNode 派生元ノードへの参照（派生アイデアの場合のみ）
 * @property tagIds ノードに付与されたタグ名の一覧。フィールド名は「tagIds」だが、
 *                  実際にはタグのIDではなくタグ名（[TagDto.name]）が格納される
 * @property reactions リアクション集計
 * @property commentCount コメント数
 * @property createdAt 作成日時（ISO 8601形式の文字列）
 * @property updatedAt 更新日時（未更新の場合null）
 */
@Serializable
data class Node(
    val id: String,
    val type: NodeType,
    val title: String,
    val content: String,
    val authorId: String,
    val authorName: String,
    val authorPicture: String? = null,
    val parentNode: ParentNode? = null,
    val tagIds: List<String> = emptyList(),
    val reactions: Reactions = Reactions(),
    val commentCount: Int = 0,
    val createdAt: String,
    val updatedAt: String? = null
)
