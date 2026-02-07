package io.github.witsisland.inspirehub.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class NodeType {
    ISSUE,
    IDEA,
    PROJECT
}
