package io.github.witsisland.inspirehub.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * API Tag レスポンス
 */
@Serializable
data class TagDto(
    val id: String,
    val name: String,
    @SerialName("usage_count")
    val usageCount: Int? = null,
    @SerialName("created_at")
    val createdAt: String = ""
)
