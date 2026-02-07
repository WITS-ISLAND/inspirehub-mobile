package io.github.witsisland.inspirehub.domain.store

import io.github.witsisland.inspirehub.domain.model.Node
import io.github.witsisland.inspirehub.domain.model.NodeType
import io.github.witsisland.inspirehub.domain.model.ReactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class HomeTab {
    RECENT, ISSUES, IDEAS, MINE
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

    private val _currentTab = MutableStateFlow(HomeTab.RECENT)
    val currentTab: StateFlow<HomeTab> = _currentTab.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.RECENT)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    fun updateNodes(nodes: List<Node>) {
        _nodes.value = nodes
    }

    fun addNode(node: Node) {
        _nodes.value = listOf(node) + _nodes.value
    }

    fun updateNode(updated: Node) {
        _nodes.value = _nodes.value.map { if (it.id == updated.id) updated else it }
        if (_selectedNode.value?.id == updated.id) {
            _selectedNode.value = updated
        }
    }

    fun removeNode(nodeId: String) {
        _nodes.value = _nodes.value.filter { it.id != nodeId }
        if (_selectedNode.value?.id == nodeId) {
            _selectedNode.value = null
        }
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
            HomeTab.RECENT -> _nodes.value
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

    /**
     * ノードのリアクション状態をメモリ上でトグルする（楽観的更新用）
     */
    fun updateNodeReaction(nodeId: String, reactionType: ReactionType) {
        _nodes.value = _nodes.value.map { node ->
            if (node.id == nodeId) toggleNodeReaction(node, reactionType) else node
        }
        _selectedNode.value?.let { selected ->
            if (selected.id == nodeId) {
                _selectedNode.value = toggleNodeReaction(selected, reactionType)
            }
        }
    }

    private fun toggleNodeReaction(node: Node, reactionType: ReactionType): Node {
        val reactions = node.reactions
        val updatedReactions = when (reactionType) {
            ReactionType.LIKE -> reactions.copy(
                like = reactions.like.copy(
                    count = if (reactions.like.isReacted) reactions.like.count - 1 else reactions.like.count + 1,
                    isReacted = !reactions.like.isReacted
                )
            )
            ReactionType.INTERESTED -> reactions.copy(
                interested = reactions.interested.copy(
                    count = if (reactions.interested.isReacted) reactions.interested.count - 1 else reactions.interested.count + 1,
                    isReacted = !reactions.interested.isReacted
                )
            )
            ReactionType.WANT_TO_TRY -> reactions.copy(
                wantToTry = reactions.wantToTry.copy(
                    count = if (reactions.wantToTry.isReacted) reactions.wantToTry.count - 1 else reactions.wantToTry.count + 1,
                    isReacted = !reactions.wantToTry.isReacted
                )
            )
        }
        return node.copy(reactions = updatedReactions)
    }

    fun clear() {
        _nodes.value = emptyList()
        _isLoading.value = false
        _selectedNode.value = null
        _currentTab.value = HomeTab.RECENT
        _sortOrder.value = SortOrder.RECENT
    }
}
