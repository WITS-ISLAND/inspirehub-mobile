package io.github.witsisland.inspirehub.data.source

import io.github.witsisland.inspirehub.data.dto.UserDto
import kotlin.native.HiddenFromObjC

/**
 * ユーザーデータソースインターフェース
 */
@HiddenFromObjC
interface UserDataSource {
    /**
     * ユーザープロフィールを更新
     * @param name 新しい表示名
     * @return 更新後のユーザー情報
     */
    suspend fun updateProfile(name: String): UserDto
}
