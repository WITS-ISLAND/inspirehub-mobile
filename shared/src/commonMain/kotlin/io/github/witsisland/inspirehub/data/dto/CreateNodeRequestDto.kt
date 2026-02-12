package io.github.witsisland.inspirehub.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * `POST /nodes` のリクエストボディDTO
 *
 * 新規ノード（課題・アイデア・プロジェクト）を作成する。
 *
 * @property title ノードのタイトル（1文字以上必須）
 * @property content ノードの本文
 * @property type ノード種別（"idea", "issue", "project" のいずれか）
 * @property tags ノードに付与するタグ名の一覧
 * @property parentNodeId 派生元ノードのID（派生アイデアの場合のみ指定）
 */
@Serializable
data class CreateNodeRequestDto(
    val title: String,
    val content: String,
    val type: String,
    val tags: List<String> = emptyList(),
    @SerialName("parent_node_id")
    val parentNodeId: String? = null
)

/**
 * `PUT /nodes/{id}` のリクエストボディDTO
 *
 * 既存ノードを更新する。Bearer認証が必要（投稿者本人のみ）。
 *
 * 注意: API仕様では全フィールドがoptionalだが、現在のDTO定義では
 * [title] と [content] がnon-nullのため、部分更新には対応していない。
 *
 * @property title 更新後のタイトル
 * @property content 更新後の本文
 * @property tags 更新後のタグ名一覧
 */
@Serializable
data class UpdateNodeRequestDto(
    val title: String,
    val content: String,
    val tags: List<String> = emptyList()
)
