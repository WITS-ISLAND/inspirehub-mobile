package io.github.witsisland.inspirehub.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Tag(
    val id: String,
    val name: String,
    val usageCount: Int = 0,
    val createdAt: String? = null
)
