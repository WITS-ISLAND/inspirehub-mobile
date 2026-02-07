package io.github.witsisland.inspirehub.presentation.viewmodel

import co.touchlab.kermit.Logger
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import com.rickclephas.kmp.observableviewmodel.MutableStateFlow
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import io.github.witsisland.inspirehub.domain.model.Node
import io.github.witsisland.inspirehub.domain.model.User
import io.github.witsisland.inspirehub.domain.repository.AuthRepository
import io.github.witsisland.inspirehub.domain.repository.NodeRepository
import io.github.witsisland.inspirehub.domain.store.UserStore
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * マイページViewModel
 */
class MyPageViewModel(
    private val userStore: UserStore,
    private val nodeRepository: NodeRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val log = Logger.withTag("MyPageViewModel")

    // UserStoreの状態をVM側のMutableStateFlowに転写
    private val _currentUser = MutableStateFlow<User?>(viewModelScope, null)
    @NativeCoroutinesState
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        viewModelScope.launch {
            userStore.currentUser.collect { _currentUser.value = it }
        }
    }

    // 自分の投稿
    private val _myNodes = MutableStateFlow(viewModelScope, emptyList<Node>())
    @NativeCoroutinesState
    val myNodes: StateFlow<List<Node>> = _myNodes.asStateFlow()

    // リアクションした投稿 (BUG-003)
    private val _reactedNodes = MutableStateFlow(viewModelScope, emptyList<Node>())
    @NativeCoroutinesState
    val reactedNodes: StateFlow<List<Node>> = _reactedNodes.asStateFlow()

    private val _isLoading = MutableStateFlow(viewModelScope, false)
    @NativeCoroutinesState
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow(viewModelScope, null as String?)
    @NativeCoroutinesState
    val error: StateFlow<String?> = _error.asStateFlow()

    // プロフィール名編集 (#26)
    private val _editingName = MutableStateFlow(viewModelScope, "")
    @NativeCoroutinesState
    val editingName: StateFlow<String> = _editingName.asStateFlow()

    private val _isEditingName = MutableStateFlow(viewModelScope, false)
    @NativeCoroutinesState
    val isEditingName: StateFlow<Boolean> = _isEditingName.asStateFlow()

    private val _isUpdatingName = MutableStateFlow(viewModelScope, false)
    @NativeCoroutinesState
    val isUpdatingName: StateFlow<Boolean> = _isUpdatingName.asStateFlow()

    fun loadMyNodes() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val userId = currentUser.value?.id
            if (userId == null) {
                _error.value = "User not authenticated"
                _isLoading.value = false
                return@launch
            }

            val result = nodeRepository.getNodes()
            if (result.isSuccess) {
                _myNodes.value = result.getOrThrow().filter { it.authorId == userId }
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to load nodes"
            }

            _isLoading.value = false
        }
    }

    /**
     * リアクション済みノードを読み込み (BUG-003)
     */
    fun loadReactedNodes() {
        viewModelScope.launch {
            _error.value = null

            val result = nodeRepository.getReactedNodes()
            if (result.isSuccess) {
                _reactedNodes.value = result.getOrThrow()
            } else {
                log.w { "Failed to load reacted nodes: ${result.exceptionOrNull()?.message}" }
            }
        }
    }

    /**
     * ログアウト (BUG-008)
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

    // --- プロフィール名編集 (#26) ---

    fun startEditingName() {
        _editingName.value = currentUser.value?.handle ?: ""
        _isEditingName.value = true
    }

    fun cancelEditingName() {
        _isEditingName.value = false
        _editingName.value = ""
    }

    fun updateEditingName(name: String) {
        _editingName.value = name
    }

    fun updateUserName() {
        val name = _editingName.value.trim()
        if (name.isBlank()) {
            _error.value = "名前を入力してください"
            return
        }

        viewModelScope.launch {
            _isUpdatingName.value = true
            _error.value = null

            val result = authRepository.updateUserName(name)
            if (result.isSuccess) {
                _isEditingName.value = false
                _editingName.value = ""
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to update name"
            }

            _isUpdatingName.value = false
        }
    }

    fun refresh() {
        loadMyNodes()
        loadReactedNodes()
    }
}
