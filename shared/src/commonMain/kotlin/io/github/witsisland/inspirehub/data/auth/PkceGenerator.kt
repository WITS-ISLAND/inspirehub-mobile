package io.github.witsisland.inspirehub.data.auth

/**
 * PKCE (Proof Key for Code Exchange) の生成ユーティリティ
 */
object PkceGenerator {

    private const val CODE_VERIFIER_LENGTH = 64
    private val CODE_VERIFIER_CHARSET = ('A'..'Z') + ('a'..'z') + ('0'..'9') + '-' + '_' + '.' + '~'

    /**
     * Code Verifierを生成
     * @return 64文字のランダムなURL-safe文字列
     */
    fun generateCodeVerifier(): String {
        return (1..CODE_VERIFIER_LENGTH)
            .map { CODE_VERIFIER_CHARSET.random() }
            .joinToString("")
    }

    /**
     * Code ChallengeをCode Verifierから生成
     * SHA256ハッシュ → Base64 URL-safeエンコード
     *
     * @param codeVerifier Code Verifier
     * @return Code Challenge (S256方式)
     */
    fun generateCodeChallenge(codeVerifier: String): String {
        return generateCodeChallengeInternal(codeVerifier)
    }
}

/**
 * Code Challenge生成の内部実装（プラットフォーム固有）
 * expect/actual でプラットフォーム別に実装
 */
internal expect fun generateCodeChallengeInternal(codeVerifier: String): String

/**
 * PKCE パラメータのペア
 */
data class PkceParams(
    val codeVerifier: String,
    val codeChallenge: String,
    val codeChallengeMethod: String = "S256"
)
