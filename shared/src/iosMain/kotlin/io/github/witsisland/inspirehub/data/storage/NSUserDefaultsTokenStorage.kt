package io.github.witsisland.inspirehub.data.storage

import io.github.witsisland.inspirehub.domain.model.User
import platform.Foundation.NSUserDefaults

/**
 * NSUserDefaultsを使用したTokenStorage実装（iOS）
 */
class NSUserDefaultsTokenStorage : TokenStorage {

    private val defaults = NSUserDefaults.standardUserDefaults

    override fun saveTokens(accessToken: String, refreshToken: String) {
        defaults.setObject(accessToken, forKey = KEY_ACCESS_TOKEN)
        defaults.setObject(refreshToken, forKey = KEY_REFRESH_TOKEN)
    }

    override fun getAccessToken(): String? {
        return defaults.stringForKey(KEY_ACCESS_TOKEN)
    }

    override fun getRefreshToken(): String? {
        return defaults.stringForKey(KEY_REFRESH_TOKEN)
    }

    override fun saveUser(user: User) {
        defaults.setObject(user.id, forKey = KEY_USER_ID)
        defaults.setObject(user.handle, forKey = KEY_USER_HANDLE)
        defaults.setObject(user.email, forKey = KEY_USER_EMAIL)
        defaults.setObject(user.picture, forKey = KEY_USER_PICTURE)
        defaults.setObject(user.roleTag, forKey = KEY_USER_ROLE_TAG)
    }

    override fun getUser(): User? {
        val id = defaults.stringForKey(KEY_USER_ID) ?: return null
        val handle = defaults.stringForKey(KEY_USER_HANDLE) ?: return null
        return User(
            id = id,
            handle = handle,
            email = defaults.stringForKey(KEY_USER_EMAIL) ?: "",
            picture = defaults.stringForKey(KEY_USER_PICTURE),
            roleTag = defaults.stringForKey(KEY_USER_ROLE_TAG)
        )
    }

    override fun clear() {
        defaults.removeObjectForKey(KEY_ACCESS_TOKEN)
        defaults.removeObjectForKey(KEY_REFRESH_TOKEN)
        defaults.removeObjectForKey(KEY_USER_ID)
        defaults.removeObjectForKey(KEY_USER_HANDLE)
        defaults.removeObjectForKey(KEY_USER_EMAIL)
        defaults.removeObjectForKey(KEY_USER_PICTURE)
        defaults.removeObjectForKey(KEY_USER_ROLE_TAG)
    }

    private companion object {
        const val KEY_ACCESS_TOKEN = "inspirehub_access_token"
        const val KEY_REFRESH_TOKEN = "inspirehub_refresh_token"
        const val KEY_USER_ID = "inspirehub_user_id"
        const val KEY_USER_HANDLE = "inspirehub_user_handle"
        const val KEY_USER_EMAIL = "inspirehub_user_email"
        const val KEY_USER_PICTURE = "inspirehub_user_picture"
        const val KEY_USER_ROLE_TAG = "inspirehub_user_role_tag"
    }
}
