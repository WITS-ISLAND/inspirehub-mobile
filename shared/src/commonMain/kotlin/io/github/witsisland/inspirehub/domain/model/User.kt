package io.github.witsisland.inspirehub.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val handle: String,
    val email: String = "",
    val picture: String? = null,
    val roleTag: String? = null
)
