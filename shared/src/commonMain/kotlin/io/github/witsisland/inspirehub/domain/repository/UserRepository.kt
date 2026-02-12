package io.github.witsisland.inspirehub.domain.repository

import io.github.witsisland.inspirehub.domain.model.User
import kotlin.native.HiddenFromObjC

/**
 * ユーザーリポジトリ
 */
@HiddenFromObjC
interface UserRepository {
    /**
     * ユーザープロフィールを更新
     * @param name 新しい表示名
     * @return 更新後のユーザー情報
     */
    suspend fun updateProfile(name: String): Result<User>
}
