package io.github.witsisland.inspirehub.data.repository

import io.github.witsisland.inspirehub.data.mapper.toDomain
import io.github.witsisland.inspirehub.data.source.UserDataSource
import io.github.witsisland.inspirehub.domain.model.User
import io.github.witsisland.inspirehub.domain.repository.UserRepository
import io.github.witsisland.inspirehub.domain.store.UserStore

class UserRepositoryImpl(
    private val userDataSource: UserDataSource,
    private val userStore: UserStore
) : UserRepository {

    override suspend fun updateProfile(name: String): Result<User> {
        return try {
            val userDto = userDataSource.updateProfile(name)
            val user = userDto.toDomain()

            // UserStore のユーザー情報を更新
            val currentAccessToken = userStore.getAccessToken() ?: ""
            val currentRefreshToken = userStore.refreshToken.value ?: ""
            userStore.login(user, currentAccessToken, currentRefreshToken)

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
