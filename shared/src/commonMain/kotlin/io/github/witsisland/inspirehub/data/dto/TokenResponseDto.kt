package io.github.witsisland.inspirehub.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 認証トークンレスポンスDTO
 *
 * `POST /auth/verify`（Google IDトークン検証）および `POST /auth/refresh`（トークンリフレッシュ）
 * の両方で使用される。verifyの場合は [user] が含まれ、refreshの場合は含まれない。
 *
 * @property accessToken APIリクエストに使用するBearerトークン
 * @property refreshToken アクセストークン失効時のリフレッシュ用トークン
 * @property expiresIn アクセストークンの有効期限（秒）
 * @property user 認証ユーザー情報。`POST /auth/verify` では返却されるが、
 *               `POST /auth/refresh` では返却されないためnullable
 * @see UserDto ユーザー情報のデータ構造
 */
@Serializable
data class TokenResponseDto(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("refresh_token")
    val refreshToken: String,
    @SerialName("expires_in")
    val expiresIn: Int,
    val user: UserDto? = null
)
