package io.github.witsisland.inspirehub.data.dto

import kotlinx.serialization.Serializable

/**
 * コメント内のメンションユーザー
 * API: { "id": string, "name": string, "picture": string | null }
 */
@Serializable
data class MentionDto(
    val id: String,
    val name: String,
    val picture: String? = null
)
