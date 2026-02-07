package io.github.witsisland.inspirehub.data.storage

import io.github.witsisland.inspirehub.domain.model.User

/**
 * TokenStorageのFake実装（テスト用）
 */
class FakeTokenStorage : TokenStorage {

    private var accessToken: String? = null
    private var refreshToken: String? = null
    private var user: User? = null

    var saveTokensCallCount = 0
    var saveUserCallCount = 0
    var clearCallCount = 0

    override fun saveTokens(accessToken: String, refreshToken: String) {
        saveTokensCallCount++
        this.accessToken = accessToken
        this.refreshToken = refreshToken
    }

    override fun getAccessToken(): String? = accessToken

    override fun getRefreshToken(): String? = refreshToken

    override fun saveUser(user: User) {
        saveUserCallCount++
        this.user = user
    }

    override fun getUser(): User? = user

    override fun clear() {
        clearCallCount++
        accessToken = null
        refreshToken = null
        user = null
    }
}
