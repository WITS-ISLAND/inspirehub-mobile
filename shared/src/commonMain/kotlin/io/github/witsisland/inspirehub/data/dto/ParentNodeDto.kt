package io.github.witsisland.inspirehub.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ParentNodeDto(
    val id: String,
    val type: String,
    val title: String,
    val content: String? = null
)
