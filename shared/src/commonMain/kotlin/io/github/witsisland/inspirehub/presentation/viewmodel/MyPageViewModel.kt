package io.github.witsisland.inspirehub.presentation.viewmodel

import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import com.rickclephas.kmp.observableviewmodel.MutableStateFlow
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import io.github.witsisland.inspirehub.domain.model.Node
import io.github.witsisland.inspirehub.domain.model.User
import io.github.witsisland.inspirehub.domain.repository.NodeRepository
import io.github.witsisland.inspirehub.domain.store.UserStore
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * マイページViewModel
 */
class MyPageViewModel(
    private val userStore: UserStore,
    private val nodeRepository: NodeRepository
) : ViewModel() {

    // UserStoreの状態をVM側のMutableStateFlowに転写
    private val _currentUser = MutableStateFlow<User?>(viewModelScope, null)
    @NativeCoroutinesState
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        viewModelScope.launch {
            userStore.currentUser.collect { _currentUser.value = it }
        }
    }

    private val _myNodes = MutableStateFlow(viewModelScope, emptyList<Node>())
    @NativeCoroutinesState
    val myNodes: StateFlow<List<Node>> = _myNodes.asStateFlow()

    private val _isLoading = MutableStateFlow(viewModelScope, false)
    @NativeCoroutinesState
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow(viewModelScope, null as String?)
    @NativeCoroutinesState
    val error: StateFlow<String?> = _error.asStateFlow()

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

    fun refresh() = loadMyNodes()
}
