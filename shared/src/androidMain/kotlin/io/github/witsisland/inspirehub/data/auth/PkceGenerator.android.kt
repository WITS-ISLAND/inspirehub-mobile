package io.github.witsisland.inspirehub.data.auth

import android.util.Base64
import java.security.MessageDigest

/**
 * Android版 PKCE Code Challenge生成
 */
internal actual fun generateCodeChallengeInternal(codeVerifier: String): String {
    val bytes = codeVerifier.toByteArray(Charsets.US_ASCII)
    val messageDigest = MessageDigest.getInstance("SHA-256")
    val digest = messageDigest.digest(bytes)

    // Base64 URL-safe エンコード（パディングなし）
    return Base64.encodeToString(digest, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
}
