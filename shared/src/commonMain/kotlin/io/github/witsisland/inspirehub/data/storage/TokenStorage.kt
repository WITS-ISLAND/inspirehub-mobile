package io.github.witsisland.inspirehub.data.storage

import io.github.witsisland.inspirehub.domain.model.User

/**
 * トークンとユーザー情報の永続化インターフェース
 */
interface TokenStorage {
    fun saveTokens(accessToken: String, refreshToken: String)
    fun getAccessToken(): String?
    fun getRefreshToken(): String?
    fun saveUser(user: User)
    fun getUser(): User?
    fun clear()
}
