package io.github.witsisland.inspirehub.data.dto

import kotlinx.serialization.Serializable

/**
 * 派生元ノードの参照DTO
 *
 * [NodeDto.parentNode] として使用される。派生アイデアが引用元のノード情報を
 * 簡易的に保持するためのデータ構造。完全なノード情報ではなく最低限のフィールドのみ。
 *
 * @property id 派生元ノードの一意識別子
 * @property type 派生元ノードの種別（"idea", "issue", "project"）
 * @property title 派生元ノードのタイトル
 * @property content 派生元ノードの本文（省略される場合null）
 * @see ParentNode ドメインモデルへの変換先
 * @see ParentNodeDto.toDomain 変換ロジック
 */
@Serializable
data class ParentNodeDto(
    val id: String,
    val type: String,
    val title: String,
    val content: String? = null
)
