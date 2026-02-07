package io.github.witsisland.inspirehub.data.source

import co.touchlab.kermit.Logger
import io.github.witsisland.inspirehub.data.dto.TokenResponseDto
import io.github.witsisland.inspirehub.data.dto.UserDto
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

/**
 * Ktor Client を使用した AuthDataSource 実装
 */
class KtorAuthDataSource(
    private val httpClient: HttpClient
) : AuthDataSource {

    private val log = Logger.withTag("KtorAuthDataSource")

    override suspend fun verifyGoogleToken(idToken: String): TokenResponseDto {
        val response = httpClient.post("/auth/verify") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("id_token" to idToken))
        }
        if (!response.status.isSuccess()) {
            val errorBody = response.bodyAsText()
            log.e { "verifyGoogleToken failed (${response.status.value}): $errorBody" }
            throw IllegalStateException("IDトークン検証失敗 (${response.status.value}): $errorBody")
        }
        return response.body()
    }

    override suspend fun refreshToken(refreshToken: String): TokenResponseDto {
        return httpClient.post("/auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("refresh_token" to refreshToken))
        }.body()
    }

    override suspend fun getCurrentUser(): UserDto {
        return httpClient.get("/auth/me").body()
    }

    override suspend fun logout() {
        httpClient.post("/auth/logout")
    }

    override suspend fun updateUserName(name: String): UserDto {
        return httpClient.patch("/users/me") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("name" to name))
        }.body()
    }
}
