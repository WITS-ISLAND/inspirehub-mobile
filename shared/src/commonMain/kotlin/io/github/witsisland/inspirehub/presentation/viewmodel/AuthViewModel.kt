package io.github.witsisland.inspirehub.presentation.viewmodel

import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import com.rickclephas.kmp.observableviewmodel.MutableStateFlow
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import io.github.witsisland.inspirehub.domain.model.User
import io.github.witsisland.inspirehub.domain.repository.AuthRepository
import io.github.witsisland.inspirehub.domain.store.UserStore
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 認証ViewModel
 */
class AuthViewModel(
    private val authRepository: AuthRepository,
    private val userStore: UserStore
) : ViewModel() {

    // UserStore の状態をVM側のMutableStateFlowに転写（KMP-ObservableViewModelの観測チェーンに乗せる）
    private val _currentUser = MutableStateFlow<User?>(viewModelScope, null)
    @NativeCoroutinesState
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(viewModelScope, false)
    @NativeCoroutinesState
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    init {
        // UserStoreのStateFlowをcollectしてVM側に転写
        viewModelScope.launch {
            userStore.currentUser.collect { _currentUser.value = it }
        }
        viewModelScope.launch {
            userStore.isAuthenticated.collect { _isAuthenticated.value = it }
        }
    }

    // 画面固有の状態
    private val _isLoading = MutableStateFlow(viewModelScope, false)
    @NativeCoroutinesState
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow(viewModelScope, null as String?)
    @NativeCoroutinesState
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _authUrl = MutableStateFlow(viewModelScope, null as String?)
    @NativeCoroutinesState
    val authUrl: StateFlow<String?> = _authUrl.asStateFlow()

    /**
     * Google OAuth URL を取得
     */
    fun getGoogleAuthUrl() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val result = authRepository.getGoogleAuthUrl()
            if (result.isSuccess) {
                _authUrl.value = result.getOrNull()
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to get auth URL"
            }

            _isLoading.value = false
        }
    }

    /**
     * OAuth認可コードでログイン
     */
    fun loginWithAuthCode(code: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val result = authRepository.loginWithAuthCode(code)
            if (result.isFailure) {
                _error.value = result.exceptionOrNull()?.message ?: "Login failed"
            }

            _isLoading.value = false
        }
    }

    /**
     * 現在のユーザー情報を取得
     */
    fun fetchCurrentUser() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val result = authRepository.getCurrentUser()
            if (result.isFailure) {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to fetch user"
            }

            _isLoading.value = false
        }
    }

    /**
     * ログアウト
     */
    fun logout() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val result = authRepository.logout()
            if (result.isFailure) {
                _error.value = result.exceptionOrNull()?.message ?: "Logout failed"
            }

            _isLoading.value = false
        }
    }

    /**
     * エラーをクリア
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * モックログイン（DEBUG用）
     * Google OAuth未設定時にモックユーザーで認証をバイパス
     */
    fun mockLogin() {
        val mockUser = User(
            id = "mock_user_1",
            handle = "テストユーザー",
            roleTag = "Engineer",
            createdAt = kotlinx.datetime.Clock.System.now()
        )
        userStore.login(mockUser, "mock_access_token", "mock_refresh_token")
    }

    /**
     * Google ID Tokenを検証してログイン（SDK方式）
     */
    fun verifyGoogleToken(idToken: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val result = authRepository.verifyGoogleToken(idToken)
            if (result.isFailure) {
                _error.value = result.exceptionOrNull()?.message ?: "Token verification failed"
            }

            _isLoading.value = false
        }
    }
}
