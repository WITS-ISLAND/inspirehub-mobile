package io.github.witsisland.inspirehub.presentation.viewmodel

import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import com.rickclephas.kmp.observableviewmodel.MutableStateFlow
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import io.github.witsisland.inspirehub.domain.model.Node
import io.github.witsisland.inspirehub.domain.model.ReactionType
import io.github.witsisland.inspirehub.domain.repository.NodeRepository
import io.github.witsisland.inspirehub.domain.repository.ReactionRepository
import io.github.witsisland.inspirehub.domain.store.HomeTab
import io.github.witsisland.inspirehub.domain.store.NodeStore
import io.github.witsisland.inspirehub.domain.store.SortOrder
import io.github.witsisland.inspirehub.domain.store.UserStore
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock

/**
 * ホーム画面ViewModel
 */
class HomeViewModel(
    private val nodeStore: NodeStore,
    private val nodeRepository: NodeRepository,
    private val reactionRepository: ReactionRepository,
    private val userStore: UserStore
) : ViewModel() {

    private val _nodes = MutableStateFlow<List<Node>>(viewModelScope, emptyList())
    @NativeCoroutinesState
    val nodes: StateFlow<List<Node>> = _nodes.asStateFlow()

    // NodeStoreの状態をVM側のMutableStateFlowに転写
    private val _isLoading = MutableStateFlow(viewModelScope, false)
    @NativeCoroutinesState
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentTab = MutableStateFlow(viewModelScope, HomeTab.RECENT)
    @NativeCoroutinesState
    val currentTab: StateFlow<HomeTab> = _currentTab.asStateFlow()

    private val _sortOrder = MutableStateFlow(viewModelScope, SortOrder.RECENT)
    @NativeCoroutinesState
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    init {
        viewModelScope.launch {
            nodeStore.isLoading.collect { _isLoading.value = it }
        }
        viewModelScope.launch {
            nodeStore.currentTab.collect { _currentTab.value = it }
        }
        viewModelScope.launch {
            nodeStore.sortOrder.collect { _sortOrder.value = it }
        }
    }

    private val _error = MutableStateFlow(viewModelScope, null as String?)
    @NativeCoroutinesState
    val error: StateFlow<String?> = _error.asStateFlow()

    /** 前回ノードをロードした時刻（epochMillis） */
    private var lastLoadedAt: Long = 0L

    /**
     * 画面表示時に呼ぶ。
     * 前回ロードから30秒以上経過していればリフレッシュ (#46)
     */
    fun onAppear() {
        val now = Clock.System.now().toEpochMilliseconds()
        if (now - lastLoadedAt > STALE_THRESHOLD_MS) {
            loadNodes(forceRefresh = true)
        }
    }

    fun loadNodes(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            nodeStore.setLoading(true)
            _error.value = null

            val result = nodeRepository.getNodes()
            if (result.isSuccess) {
                nodeStore.updateNodes(result.getOrThrow())
                lastLoadedAt = Clock.System.now().toEpochMilliseconds()
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to load nodes"
            }

            applyFilter()
            nodeStore.setLoading(false)
        }
    }

    fun refresh() = loadNodes(forceRefresh = true)

    fun setTab(tab: HomeTab) {
        nodeStore.setTab(tab)
        applyFilter()
    }

    fun setSortOrder(order: SortOrder) {
        nodeStore.setSortOrder(order)
        applyFilter()
    }

    private fun applyFilter() {
        val currentUserId = userStore.currentUser.value?.id
        _nodes.value = nodeStore.getFilteredNodes(currentUserId)
    }

    fun toggleReaction(nodeId: String, type: ReactionType) {
        // 1. 即座にUI更新（楽観的更新）
        nodeStore.updateNodeReaction(nodeId, type)
        applyFilter()

        // 2. バックグラウンドでAPI呼び出し
        viewModelScope.launch {
            val result = reactionRepository.toggleReaction(nodeId, type)
            if (result.isFailure) {
                // 3. 失敗時はロールバック（再度トグルで元に戻す）
                nodeStore.updateNodeReaction(nodeId, type)
                applyFilter()
                _error.value = result.exceptionOrNull()?.message ?: "Failed to toggle reaction"
            }
        }
    }

    companion object {
        /** 30秒間はキャッシュを使い、超えたらリフレッシュ */
        private const val STALE_THRESHOLD_MS = 30_000L
    }
}
