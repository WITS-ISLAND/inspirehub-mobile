package io.github.witsisland.inspirehub.domain.store

import io.github.witsisland.inspirehub.domain.model.Node
import io.github.witsisland.inspirehub.domain.model.NodeType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class HomeTab {
    ALL, ISSUES, IDEAS, MINE
}

enum class SortOrder {
    RECENT, POPULAR
}

class NodeStore {
    private val _nodes = MutableStateFlow<List<Node>>(emptyList())
    val nodes: StateFlow<List<Node>> = _nodes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedNode = MutableStateFlow<Node?>(null)
    val selectedNode: StateFlow<Node?> = _selectedNode.asStateFlow()

    private val _currentTab = MutableStateFlow(HomeTab.ALL)
    val currentTab: StateFlow<HomeTab> = _currentTab.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.RECENT)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    fun updateNodes(nodes: List<Node>) {
        _nodes.value = nodes
    }

    fun addNode(node: Node) {
        _nodes.value = listOf(node) + _nodes.value
    }

    fun selectNode(node: Node?) {
        _selectedNode.value = node
    }

    fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }

    fun setTab(tab: HomeTab) {
        _currentTab.value = tab
    }

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }

    fun getFilteredNodes(currentUserId: String? = null): List<Node> {
        val filtered = when (_currentTab.value) {
            HomeTab.ALL -> _nodes.value
            HomeTab.ISSUES -> _nodes.value.filter { it.type == NodeType.ISSUE }
            HomeTab.IDEAS -> _nodes.value.filter { it.type == NodeType.IDEA }
            HomeTab.MINE -> if (currentUserId != null) {
                _nodes.value.filter { it.authorId == currentUserId }
            } else {
                _nodes.value
            }
        }
        return when (_sortOrder.value) {
            SortOrder.RECENT -> filtered.sortedByDescending { it.createdAt }
            SortOrder.POPULAR -> filtered.sortedByDescending { node ->
                node.reactions.like.count +
                    node.reactions.interested.count +
                    node.reactions.wantToTry.count
            }
        }
    }

    fun clear() {
        _nodes.value = emptyList()
        _isLoading.value = false
        _selectedNode.value = null
        _currentTab.value = HomeTab.ALL
        _sortOrder.value = SortOrder.RECENT
    }
}
