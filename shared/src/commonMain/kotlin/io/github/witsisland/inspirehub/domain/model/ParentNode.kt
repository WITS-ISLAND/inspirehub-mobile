package io.github.witsisland.inspirehub.domain.model

import kotlinx.serialization.Serializable

/**
 * 派生元ノードへの参照のドメインモデル
 *
 * 派生アイデアが引用元の課題やアイデアを参照するための簡易データ。
 * 完全なノード情報（リアクション、コメント数等）は含まず、表示に必要な最低限のフィールドのみ。
 *
 * @property id 派生元ノードの一意識別子
 * @property type 派生元ノードの種別
 * @property title 派生元ノードのタイトル
 * @property content 派生元ノードの本文（省略される場合null）
 */
@Serializable
data class ParentNode(
    val id: String,
    val type: NodeType,
    val title: String,
    val content: String? = null
)
