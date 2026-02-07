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

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val userStore: UserStore
) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(viewModelScope, null)
    @NativeCoroutinesState
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(viewModelScope, false)
    @NativeCoroutinesState
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    init {
        viewModelScope.launch {
            userStore.currentUser.collect { _currentUser.value = it }
        }
        viewModelScope.launch {
            userStore.isAuthenticated.collect { _isAuthenticated.value = it }
        }
    }

    private val _isLoading = MutableStateFlow(viewModelScope, false)
    @NativeCoroutinesState
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow(viewModelScope, null as String?)
    @NativeCoroutinesState
    val error: StateFlow<String?> = _error.asStateFlow()

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

    fun clearError() {
        _error.value = null
    }

    fun mockLogin() {
        val mockUser = User(
            id = "mock_user_1",
            handle = "テストユーザー",
            email = "test@example.com",
            picture = null,
            roleTag = "Engineer"
        )
        userStore.login(mockUser, "mock_access_token", "mock_refresh_token")
    }

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
