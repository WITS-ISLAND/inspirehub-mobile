package io.github.witsisland.inspirehub.presentation.viewmodel

import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import com.rickclephas.kmp.observableviewmodel.MutableStateFlow
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import io.github.witsisland.inspirehub.domain.model.ReactedUser
import io.github.witsisland.inspirehub.domain.model.ReactionType
import io.github.witsisland.inspirehub.domain.repository.ReactionRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * リアクションユーザー一覧ViewModel
 *
 * 指定ノード・リアクション種別のユーザー一覧を取得し、
 * カーソルベースのページネーションで追加読み込みをサポートする。
 */
class ReactionUsersViewModel(
    private val reactionRepository: ReactionRepository
) : ViewModel() {

    private val _users = MutableStateFlow(viewModelScope, emptyList<ReactedUser>())
    @NativeCoroutinesState
    val users: StateFlow<List<ReactedUser>> = _users.asStateFlow()

    private val _isLoading = MutableStateFlow(viewModelScope, false)
    @NativeCoroutinesState
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(viewModelScope, false)
    @NativeCoroutinesState
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _error = MutableStateFlow(viewModelScope, null as String?)
    @NativeCoroutinesState
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _hasMore = MutableStateFlow(viewModelScope, false)
    @NativeCoroutinesState
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()

    private val _total = MutableStateFlow(viewModelScope, 0)
    @NativeCoroutinesState
    val total: StateFlow<Int> = _total.asStateFlow()

    private var currentNodeId: String? = null
    private var currentType: ReactionType? = null
    private var nextCursor: String? = null

    /**
     * リアクションユーザー一覧を初回ロード
     *
     * @param nodeId 対象ノードID
     * @param type リアクション種別
     */
    fun loadUsers(nodeId: String, type: ReactionType) {
        currentNodeId = nodeId
        currentType = type
        nextCursor = null

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _users.value = emptyList()

            val result = reactionRepository.getReactedUsers(
                nodeId = nodeId,
                type = type,
                limit = 30,
                cursor = null
            )

            if (result.isSuccess) {
                val page = result.getOrNull()!!
                _users.value = page.users
                _hasMore.value = page.hasMore
                _total.value = page.total
                nextCursor = page.nextCursor
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to load reaction users"
            }

            _isLoading.value = false
        }
    }

    /**
     * 追加ページを読み込む（無限スクロール用）
     *
     * 既にロード中の場合や次ページが存在しない場合は何もしない。
     */
    fun loadMore() {
        if (_isLoadingMore.value || !_hasMore.value) return
        val nodeId = currentNodeId ?: return
        val type = currentType ?: return
        val cursor = nextCursor ?: return

        viewModelScope.launch {
            _isLoadingMore.value = true

            val result = reactionRepository.getReactedUsers(
                nodeId = nodeId,
                type = type,
                limit = 30,
                cursor = cursor
            )

            if (result.isSuccess) {
                val page = result.getOrNull()!!
                _users.value = _users.value + page.users
                _hasMore.value = page.hasMore
                _total.value = page.total
                nextCursor = page.nextCursor
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to load more reaction users"
            }

            _isLoadingMore.value = false
        }
    }
}
