package io.github.witsisland.inspirehub.presentation.viewmodel

import com.rickclephas.kmp.observableviewmodel.MutableStateFlow
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import io.github.witsisland.inspirehub.domain.model.Node
import io.github.witsisland.inspirehub.domain.model.NodeType
import io.github.witsisland.inspirehub.domain.repository.NodeRepository
import io.github.witsisland.inspirehub.domain.store.HomeTab
import io.github.witsisland.inspirehub.domain.store.NodeStore
import io.github.witsisland.inspirehub.domain.store.SortOrder
import io.github.witsisland.inspirehub.domain.store.UserStore
import kotlinx.coroutines.flow.StateFlow

/**
 * ホーム画面ViewModel
 */
class HomeViewModel(
    private val nodeStore: NodeStore,
    private val nodeRepository: NodeRepository,
    private val userStore: UserStore
) : ViewModel() {

    val nodes = MutableStateFlow<List<Node>>(viewModelScope, emptyList())

    val isLoading: StateFlow<Boolean> = nodeStore.isLoading

    val currentTab: StateFlow<HomeTab> = nodeStore.currentTab

    val sortOrder: StateFlow<SortOrder> = nodeStore.sortOrder

    val error = MutableStateFlow(viewModelScope, null as String?)

    fun loadNodes(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            nodeStore.setLoading(true)
            error.value = null

            val result = nodeRepository.getNodes()
            if (result.isSuccess) {
                nodeStore.updateNodes(result.getOrThrow())
            } else {
                error.value = result.exceptionOrNull()?.message ?: "Failed to load nodes"
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
        val allNodes = nodeStore.nodes.value
        val tab = nodeStore.currentTab.value
        val order = nodeStore.sortOrder.value

        val filtered = when (tab) {
            HomeTab.RECENT -> allNodes
            HomeTab.ISSUES -> allNodes.filter { it.type == NodeType.ISSUE }
            HomeTab.IDEAS -> allNodes.filter { it.type == NodeType.IDEA }
            HomeTab.MINE -> allNodes
        }
        nodes.value = when (order) {
            SortOrder.RECENT -> filtered.sortedByDescending { it.createdAt }
            SortOrder.POPULAR -> filtered
        }
    }

    fun toggleLike(nodeId: String) {
        viewModelScope.launch {
            val result = nodeRepository.toggleLike(nodeId)
            if (result.isSuccess) {
                loadNodes()
            } else {
                error.value = result.exceptionOrNull()?.message ?: "Failed to toggle like"
            }
        }
    }
}
