package io.github.witsisland.inspirehub.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * POST /nodes リクエストボディ
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
 * PUT /nodes/{id} リクエストボディ
 */
@Serializable
data class UpdateNodeRequestDto(
    val title: String,
    val content: String,
    val tags: List<String> = emptyList()
)
