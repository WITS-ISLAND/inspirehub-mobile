package io.github.witsisland.inspirehub.data.network

import co.touchlab.kermit.Logger as KermitLogger
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * HttpClient のファクトリー
 * プラットフォーム固有のエンジンを expect/actual で注入
 */
expect fun createHttpClient(
    baseUrl: String = "https://api.inspirehub.wtnqk.org",
    enableLogging: Boolean = true,
    tokenProvider: (() -> String?)? = null,
    refreshTokenProvider: (() -> String?)? = null,
    onTokenRefreshed: ((accessToken: String, refreshToken: String) -> Unit)? = null
): HttpClient

/**
 * 共通の HttpClient 設定
 */
fun HttpClient.configureClient(
    baseUrl: String,
    enableLogging: Boolean,
    tokenProvider: (() -> String?)? = null,
    refreshTokenProvider: (() -> String?)? = null,
    onTokenRefreshed: ((accessToken: String, refreshToken: String) -> Unit)? = null
): HttpClient {
    return this.config {
        // ベースURL設定
        defaultRequest {
            url(baseUrl)
        }

        // JSON シリアライズ設定
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }

        // ログ設定（Kermit経由）
        if (enableLogging) {
            val log = KermitLogger.withTag("HttpClient")
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        log.d { message }
                    }
                }
                level = LogLevel.INFO
            }
        }

        // 認証設定
        // 認証設定: Bearer tokenが利用可能ならGET含む全リクエストに付与
        // 認証済み → is_reacted等のユーザー固有情報を返す
        // 未認証 → is_reacted=false として返す
        tokenProvider?.let { provider ->
            install(Auth) {
                bearer {
                    loadTokens {
                        provider()?.let { token ->
                            val rt = refreshTokenProvider?.invoke() ?: ""
                            BearerTokens(accessToken = token, refreshToken = rt)
                        }
                    }
                    refreshTokens {
                        val rt = oldTokens?.refreshToken?.takeIf { it.isNotEmpty() }
                            ?: return@refreshTokens null
                        try {
                            val response = client.post("${baseUrl}/auth/refresh") {
                                contentType(ContentType.Application.Json)
                                setBody("""{"refresh_token":"$rt"}""")
                            }
                            if (!response.status.isSuccess()) {
                                return@refreshTokens null
                            }
                            val json = Json { ignoreUnknownKeys = true }
                            val tokenResponse = json.decodeFromString<RefreshTokenResponse>(response.bodyAsText())
                            onTokenRefreshed?.invoke(tokenResponse.accessToken, tokenResponse.refreshToken)
                            BearerTokens(tokenResponse.accessToken, tokenResponse.refreshToken)
                        } catch (_: Exception) {
                            null
                        }
                    }
                    sendWithoutRequest { true }
                }
            }
        }

        // レスポンスバリデーション: 非2xxレスポンスを例外に変換
        // サーバーがtext/plainでエラーを返す場合のNoTransformationFoundExceptionを防止
        HttpResponseValidator {
            validateResponse { response ->
                val statusCode = response.status.value
                if (statusCode >= 400) {
                    val body = response.bodyAsText()
                    throw when (statusCode) {
                        401 -> ApiException.Unauthorized(body)
                        403 -> ApiException.Forbidden(body)
                        404 -> ApiException.NotFound(body)
                        else -> ApiException.ServerError(statusCode, body)
                    }
                }
            }
        }

        // タイムアウト設定
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 30_000
        }
    }
}

/**
 * API エラーの例外クラス
 * サーバーが非2xxレスポンスを返した場合に使用
 */
sealed class ApiException(message: String) : Exception(message) {
    class Unauthorized(body: String) : ApiException("認証が必要です: $body")
    class Forbidden(body: String) : ApiException("権限がありません: $body")
    class NotFound(body: String) : ApiException("リソースが見つかりません: $body")
    class ServerError(code: Int, body: String) : ApiException("サーバーエラー ($code): $body")
}

@Serializable
private data class RefreshTokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String
)
