package io.github.witsisland.inspirehub.data.source

import io.github.witsisland.inspirehub.data.auth.PkceGenerator
import io.github.witsisland.inspirehub.data.dto.TokenResponseDto
import io.github.witsisland.inspirehub.data.dto.UserDto
import io.github.witsisland.inspirehub.domain.store.UserStore
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * Ktor Client を使用した AuthDataSource 実装
 */
class KtorAuthDataSource(
    private val httpClient: HttpClient,
    private val userStore: UserStore
) : AuthDataSource {

    companion object {
        // TODO: 環境に応じて変更する必要がある
        private const val REDIRECT_URI = "inspirehub://auth/callback"
    }

    override suspend fun getGoogleAuthUrl(): String {
        // PKCE パラメータを生成
        val codeVerifier = PkceGenerator.generateCodeVerifier()
        val codeChallenge = PkceGenerator.generateCodeChallenge(codeVerifier)

        // デバッグログ
        println("=== PKCE Parameters ===")
        println("code_verifier: $codeVerifier")
        println("code_challenge: $codeChallenge")
        println("redirect_uri: $REDIRECT_URI")

        // code_verifierを保存（後でトークン交換時に使用）
        userStore.saveCodeVerifier(codeVerifier)

        // APIリクエスト
        val response: Map<String, String> = httpClient.get("/auth/google/url") {
            parameter("code_challenge", codeChallenge)
            parameter("code_challenge_method", "S256")
            parameter("redirect_uri", REDIRECT_URI)
        }.body()

        println("=== API Response ===")
        println("response: $response")
        val url = response["url"] ?: throw IllegalStateException("OAuth URL not found in response")
        println("OAuth URL: $url")

        return url
    }

    override suspend fun exchangeAuthCode(code: String): TokenResponseDto {
        return httpClient.get("/auth/google/callback") {
            parameter("code", code)
        }.body()
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
}
