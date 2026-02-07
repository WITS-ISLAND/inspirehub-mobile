package io.github.witsisland.inspirehub.data.repository

import co.touchlab.kermit.Logger
import io.github.witsisland.inspirehub.data.mapper.toDomain
import io.github.witsisland.inspirehub.data.source.AuthDataSource
import io.github.witsisland.inspirehub.domain.model.User
import io.github.witsisland.inspirehub.domain.repository.AuthRepository
import io.github.witsisland.inspirehub.domain.store.UserStore

/**
 * AuthRepository の実装
 */
class AuthRepositoryImpl(
    private val authDataSource: AuthDataSource,
    private val userStore: UserStore
) : AuthRepository {

    private val log = Logger.withTag("AuthRepositoryImpl")

    override suspend fun verifyGoogleToken(idToken: String): Result<User> {
        return try {
            val tokenResponse = authDataSource.verifyGoogleToken(idToken)
            val user = tokenResponse.user.toDomain()

            // UserStore にログイン状態を保存
            userStore.login(
                user = user,
                accessToken = tokenResponse.accessToken,
                refreshToken = tokenResponse.refreshToken
            )

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

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            authDataSource.logout()
            userStore.logout()
            Result.success(Unit)
        } catch (e: Exception) {
            // ログアウトAPIが失敗してもローカルの状態はクリア
            userStore.logout()
            Result.failure(e)
        }
    }
}
