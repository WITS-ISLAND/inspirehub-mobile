package io.github.witsisland.inspirehub.data.storage

import android.content.Context
import io.github.witsisland.inspirehub.domain.model.User

/**
 * SharedPreferencesを使用したTokenStorage実装（Android）
 */
class SharedPreferencesTokenStorage(context: Context) : TokenStorage {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .apply()
    }

    override fun getAccessToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    override fun getRefreshToken(): String? {
        return prefs.getString(KEY_REFRESH_TOKEN, null)
    }

    override fun saveUser(user: User) {
        prefs.edit()
            .putString(KEY_USER_ID, user.id)
            .putString(KEY_USER_HANDLE, user.handle)
            .putString(KEY_USER_EMAIL, user.email)
            .putString(KEY_USER_PICTURE, user.picture)
            .putString(KEY_USER_ROLE_TAG, user.roleTag)
            .apply()
    }

    override fun getUser(): User? {
        val id = prefs.getString(KEY_USER_ID, null) ?: return null
        val handle = prefs.getString(KEY_USER_HANDLE, null) ?: return null
        return User(
            id = id,
            handle = handle,
            email = prefs.getString(KEY_USER_EMAIL, null) ?: "",
            picture = prefs.getString(KEY_USER_PICTURE, null),
            roleTag = prefs.getString(KEY_USER_ROLE_TAG, null)
        )
    }

    override fun clear() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val PREFS_NAME = "inspirehub_auth"
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val KEY_USER_ID = "user_id"
        const val KEY_USER_HANDLE = "user_handle"
        const val KEY_USER_EMAIL = "user_email"
        const val KEY_USER_PICTURE = "user_picture"
        const val KEY_USER_ROLE_TAG = "user_role_tag"
    }
}
