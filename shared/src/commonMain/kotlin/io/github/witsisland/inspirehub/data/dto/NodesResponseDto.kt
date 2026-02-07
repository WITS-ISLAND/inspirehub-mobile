package io.github.witsisland.inspirehub.data.dto

import kotlinx.serialization.Serializable

/**
 * GET /nodes のレスポンスラッパー
 */
@Serializable
data class NodesResponseDto(
    val nodes: List<NodeDto>,
    val total: Int
)
