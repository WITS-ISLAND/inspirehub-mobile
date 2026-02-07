package io.github.witsisland.inspirehub.domain.store

import io.github.witsisland.inspirehub.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ユーザー認証状態を管理するStore
 * シングルトンで画面を跨いでユーザー情報とトークンを保持
 */
class UserStore {
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _accessToken = MutableStateFlow<String?>(null)
    val accessToken: StateFlow<String?> = _accessToken.asStateFlow()

    private val _refreshToken = MutableStateFlow<String?>(null)
    val refreshToken: StateFlow<String?> = _refreshToken.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    /**
     * ログイン状態を設定
     */
    fun login(user: User, accessToken: String, refreshToken: String) {
        _currentUser.value = user
        _accessToken.value = accessToken
        _refreshToken.value = refreshToken
        _isAuthenticated.value = true
    }

    /**
     * アクセストークンを更新
     */
    fun updateAccessToken(accessToken: String) {
        _accessToken.value = accessToken
    }

    /**
     * ログアウト（状態をクリア）
     */
    fun logout() {
        _currentUser.value = null
        _accessToken.value = null
        _refreshToken.value = null
        _isAuthenticated.value = false
    }

    /**
     * ユーザー情報を更新（名前変更等）
     */
    fun updateUser(user: User) {
        _currentUser.value = user
    }

    /**
     * 現在のアクセストークンを取得（nullable）
     */
    fun getAccessToken(): String? = _accessToken.value
}
