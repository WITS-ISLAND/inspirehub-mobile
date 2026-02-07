package io.github.witsisland.inspirehub.data.network

import io.ktor.client.*
import io.ktor.client.engine.darwin.*

/**
 * iOS用 HttpClient 実装（Darwin エンジン）
 */
actual fun createHttpClient(
    baseUrl: String,
    enableLogging: Boolean,
    tokenProvider: (() -> String?)?,
    refreshTokenProvider: (() -> String?)?,
    onTokenRefreshed: ((accessToken: String, refreshToken: String) -> Unit)?
): HttpClient {
    return HttpClient(Darwin).configureClient(
        baseUrl = baseUrl,
        enableLogging = enableLogging,
        tokenProvider = tokenProvider,
        refreshTokenProvider = refreshTokenProvider,
        onTokenRefreshed = onTokenRefreshed
    )
}
