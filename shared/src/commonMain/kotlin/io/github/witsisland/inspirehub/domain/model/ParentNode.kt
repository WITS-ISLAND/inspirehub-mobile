package io.github.witsisland.inspirehub.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ParentNode(
    val id: String,
    val type: NodeType,
    val title: String
)
