package io.github.witsisland.inspirehub.data.repository

import co.touchlab.kermit.Logger
import io.github.witsisland.inspirehub.data.mapper.toDomain
import io.github.witsisland.inspirehub.data.source.AuthDataSource
import io.github.witsisland.inspirehub.data.storage.TokenStorage
import io.github.witsisland.inspirehub.domain.model.User
import io.github.witsisland.inspirehub.domain.repository.AuthRepository
import io.github.witsisland.inspirehub.domain.store.UserStore

/**
 * AuthRepository の実装
 */
class AuthRepositoryImpl(
    private val authDataSource: AuthDataSource,
    private val userStore: UserStore,
    private val tokenStorage: TokenStorage
) : AuthRepository {

    private val log = Logger.withTag("AuthRepositoryImpl")

    override suspend fun verifyGoogleToken(idToken: String): Result<User> {
        return try {
            val tokenResponse = authDataSource.verifyGoogleToken(idToken)
            val userDto = tokenResponse.user
                ?: return Result.failure(IllegalStateException("ユーザー情報が含まれていません"))
            val user = userDto.toDomain()

            log.d { "Access token: ${tokenResponse.accessToken}" }

            // UserStore にログイン状態を保存
            userStore.login(
                user = user,
                accessToken = tokenResponse.accessToken,
                refreshToken = tokenResponse.refreshToken
            )

            // 永続化
            tokenStorage.saveTokens(tokenResponse.accessToken, tokenResponse.refreshToken)
            tokenStorage.saveUser(user)

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun refreshAccessToken(): Result<Unit> {
        return try {
            val currentRefreshToken = userStore.refreshToken.value
                ?: return Result.failure(IllegalStateException("No refresh token available"))

            val tokenResponse = authDataSource.refreshToken(currentRefreshToken)

            // アクセストークンを更新
            userStore.updateAccessToken(tokenResponse.accessToken)

            // 永続化も更新
            tokenStorage.saveTokens(tokenResponse.accessToken, currentRefreshToken)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUser(): Result<User> {
        return try {
            val userDto = authDataSource.getCurrentUser()
            val user = userDto.toDomain()

            // UserStore を更新（最新のユーザー情報で）
            userStore.currentUser.value?.let {
                userStore.login(
                    user = user,
                    accessToken = userStore.accessToken.value ?: "",
                    refreshToken = userStore.refreshToken.value ?: ""
                )
            }

            // 永続化のユーザー情報も更新
            tokenStorage.saveUser(user)

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            authDataSource.logout()
            userStore.logout()
            tokenStorage.clear()
            Result.success(Unit)
        } catch (e: Exception) {
            // ログアウトAPIが失敗してもローカルの状態はクリア
            userStore.logout()
            tokenStorage.clear()
            Result.failure(e)
        }
    }

    override suspend fun updateUserName(name: String): Result<User> {
        return try {
            val userDto = authDataSource.updateUserName(name)
            val user = userDto.toDomain()
            userStore.updateUser(user)
            tokenStorage.saveUser(user)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun restoreSession(): Result<User?> {
        return try {
            val accessToken = tokenStorage.getAccessToken()
            val refreshToken = tokenStorage.getRefreshToken()
            val user = tokenStorage.getUser()

            if (accessToken != null && refreshToken != null && user != null) {
                log.d { "Restoring session for user: ${user.handle}" }

                // UserStore にセッションを復元
                userStore.login(
                    user = user,
                    accessToken = accessToken,
                    refreshToken = refreshToken
                )
                Result.success(user)
            } else {
                log.d { "No saved session found" }
                Result.success(null)
            }
        } catch (e: Exception) {
            log.e(e) { "Failed to restore session" }
            Result.failure(e)
        }
    }
}
