package io.github.witsisland.inspirehub.data.dto

import kotlinx.serialization.Serializable

/**
 * `GET /nodes` のレスポンスDTO
 *
 * ノード一覧をページネーション付きで返すAPIレスポンスのラッパー。
 * クエリパラメータ: type, author_id, parent_node_id, tag, q, sort, limit, offset
 *
 * @property nodes ノード一覧
 * @property total 検索条件に合致するノードの総数（ページネーション用）
 * @see NodeDto 個別ノードのデータ構造
 */
@Serializable
data class NodesResponseDto(
    val nodes: List<NodeDto>,
    val total: Int
)
