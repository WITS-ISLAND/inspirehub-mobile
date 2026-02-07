package io.github.witsisland.inspirehub.data.network

import co.touchlab.kermit.Logger as KermitLogger
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * HttpClient のファクトリー
 * プラットフォーム固有のエンジンを expect/actual で注入
 */
expect fun createHttpClient(
    baseUrl: String = "https://api.inspirehub.wtnqk.org",
    enableLogging: Boolean = true,
    tokenProvider: (() -> String?)? = null
): HttpClient

/**
 * 共通の HttpClient 設定
 */
fun HttpClient.configureClient(
    baseUrl: String,
    enableLogging: Boolean,
    tokenProvider: (() -> String?)? = null
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
                            BearerTokens(accessToken = token, refreshToken = "")
                        }
                    }
                    sendWithoutRequest { true }
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
