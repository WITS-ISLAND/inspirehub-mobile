package io.github.witsisland.inspirehub.domain.repository

import io.github.witsisland.inspirehub.domain.model.User
import kotlin.native.HiddenFromObjC

/**
 * AuthRepositoryのFake実装（テスト用）
 */
@HiddenFromObjC
class FakeAuthRepository : AuthRepository {

    var verifyGoogleTokenResult: Result<User>? = null
    var refreshAccessTokenResult: Result<Unit> = Result.success(Unit)
    var getCurrentUserResult: Result<User>? = null
    var logoutResult: Result<Unit> = Result.success(Unit)
    var updateUserNameResult: Result<User>? = null
    var restoreSessionResult: Result<User?> = Result.success(null)

    // 呼び出し回数をカウント
    var verifyGoogleTokenCallCount = 0
    var refreshAccessTokenCallCount = 0
    var getCurrentUserCallCount = 0
    var logoutCallCount = 0
    var updateUserNameCallCount = 0
    var restoreSessionCallCount = 0

    // 最後に渡された引数を保存
    var lastIdToken: String? = null
    var lastUpdateUserName: String? = null

    override suspend fun verifyGoogleToken(idToken: String): Result<User> {
        verifyGoogleTokenCallCount++
        lastIdToken = idToken
        return verifyGoogleTokenResult ?: error("verifyGoogleTokenResult not set")
    }

    override suspend fun refreshAccessToken(): Result<Unit> {
        refreshAccessTokenCallCount++
        return refreshAccessTokenResult
    }

    override suspend fun getCurrentUser(): Result<User> {
        getCurrentUserCallCount++
        return getCurrentUserResult ?: error("getCurrentUserResult not set")
    }

    override suspend fun logout(): Result<Unit> {
        logoutCallCount++
        return logoutResult
    }

    override suspend fun updateUserName(name: String): Result<User> {
        updateUserNameCallCount++
        lastUpdateUserName = name
        return updateUserNameResult ?: error("updateUserNameResult not set")
    }

    override suspend fun restoreSession(): Result<User?> {
        restoreSessionCallCount++
        return restoreSessionResult
    }

    fun reset() {
        verifyGoogleTokenCallCount = 0
        refreshAccessTokenCallCount = 0
        getCurrentUserCallCount = 0
        logoutCallCount = 0
        updateUserNameCallCount = 0
        restoreSessionCallCount = 0
        lastIdToken = null
        lastUpdateUserName = null
    }
}
