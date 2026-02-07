package io.github.witsisland.inspirehub.presentation.viewmodel

import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import com.rickclephas.kmp.observableviewmodel.MutableStateFlow
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import io.github.witsisland.inspirehub.domain.model.Node
import io.github.witsisland.inspirehub.domain.repository.NodeRepository
import io.github.witsisland.inspirehub.domain.store.NodeStore
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MapViewModel(
    private val nodeStore: NodeStore,
    private val nodeRepository: NodeRepository
) : ViewModel() {

    private val _nodes = MutableStateFlow<List<Node>>(viewModelScope, emptyList())
    @NativeCoroutinesState
    val nodes: StateFlow<List<Node>> = _nodes.asStateFlow()

    private val _isLoading = MutableStateFlow(viewModelScope, false)
    @NativeCoroutinesState
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        viewModelScope.launch {
            nodeStore.nodes.collect { _nodes.value = it }
        }
        viewModelScope.launch {
            nodeStore.isLoading.collect { _isLoading.value = it }
        }
    }

    private val _error = MutableStateFlow(viewModelScope, null as String?)
    @NativeCoroutinesState
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadNodes() {
        viewModelScope.launch {
            nodeStore.setLoading(true)
            _error.value = null

            val result = nodeRepository.getNodes()
            if (result.isSuccess) {
                nodeStore.updateNodes(result.getOrThrow())
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to load nodes"
            }

            nodeStore.setLoading(false)
        }
    }

    fun getNodeTree(): List<NodeTreeItem> {
        val allNodes = nodes.value
        val childrenMap = allNodes.groupBy { it.parentNode?.id }
        val result = mutableListOf<NodeTreeItem>()

        fun buildTree(parentId: String?, depth: Int) {
            val children = childrenMap[parentId] ?: return
            for (node in children) {
                val childCount = childrenMap[node.id]?.size ?: 0
                result.add(NodeTreeItem(node = node, depth = depth, childCount = childCount))
                buildTree(node.id, depth + 1)
            }
        }

        buildTree(null, 0)
        return result
    }
}

data class NodeTreeItem(
    val node: Node,
    val depth: Int,
    val childCount: Int
)
