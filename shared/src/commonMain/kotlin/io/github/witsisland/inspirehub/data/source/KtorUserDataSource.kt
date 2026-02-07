package io.github.witsisland.inspirehub.data.source

import io.github.witsisland.inspirehub.data.dto.UserDto
import io.github.witsisland.inspirehub.data.dto.UserUpdateResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * Ktor Client を使用した UserDataSource 実装
 */
class KtorUserDataSource(
    private val httpClient: HttpClient
) : UserDataSource {

    override suspend fun updateProfile(name: String): UserDto {
        val response: UserUpdateResponseDto = httpClient.patch("/users/me") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("name" to name))
        }.body()
        return response.user
    }
}
