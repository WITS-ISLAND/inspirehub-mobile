package io.github.witsisland.inspirehub.presentation.viewmodel

import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import com.rickclephas.kmp.observableviewmodel.MutableStateFlow
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import io.github.witsisland.inspirehub.domain.model.Comment
import io.github.witsisland.inspirehub.domain.model.Node
import io.github.witsisland.inspirehub.domain.model.ReactionType
import io.github.witsisland.inspirehub.domain.repository.CommentRepository
import io.github.witsisland.inspirehub.domain.repository.NodeRepository
import io.github.witsisland.inspirehub.domain.repository.ReactionRepository
import io.github.witsisland.inspirehub.domain.store.NodeStore
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 詳細ViewModel
 * ノード詳細・コメント・子ノード表示を管理
 */
class DetailViewModel(
    private val nodeStore: NodeStore,
    private val nodeRepository: NodeRepository,
    private val commentRepository: CommentRepository,
    private val reactionRepository: ReactionRepository
) : ViewModel() {

    // NodeStoreの選択状態をVM側のMutableStateFlowに転写
    private val _selectedNode = MutableStateFlow<Node?>(viewModelScope, null)
    @NativeCoroutinesState
    val selectedNode: StateFlow<Node?> = _selectedNode.asStateFlow()

    init {
        viewModelScope.launch {
            nodeStore.selectedNode.collect { _selectedNode.value = it }
        }
    }

    private val _comments = MutableStateFlow(viewModelScope, emptyList<Comment>())
    @NativeCoroutinesState
    val comments: StateFlow<List<Comment>> = _comments.asStateFlow()

    private val _childNodes = MutableStateFlow(viewModelScope, emptyList<Node>())
    @NativeCoroutinesState
    val childNodes: StateFlow<List<Node>> = _childNodes.asStateFlow()

    private val _isLoading = MutableStateFlow(viewModelScope, false)
    @NativeCoroutinesState
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow(viewModelScope, null as String?)
    @NativeCoroutinesState
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _commentText = MutableStateFlow(viewModelScope, "")
    @NativeCoroutinesState
    val commentText: StateFlow<String> = _commentText.asStateFlow()

    private val _isCommentSubmitting = MutableStateFlow(viewModelScope, false)
    @NativeCoroutinesState
    val isCommentSubmitting: StateFlow<Boolean> = _isCommentSubmitting.asStateFlow()

    /**
     * ノード詳細を読み込み
     * getNode + getComments + getChildNodes を並行実行
     */
    fun loadDetail(nodeId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val nodeDeferred = async { nodeRepository.getNode(nodeId) }
            val commentsDeferred = async { commentRepository.getComments(nodeId) }
            val childNodesDeferred = async { nodeRepository.getChildNodes(nodeId) }

            val nodeResult = nodeDeferred.await()
            val commentsResult = commentsDeferred.await()
            val childNodesResult = childNodesDeferred.await()

            if (nodeResult.isSuccess) {
                nodeStore.selectNode(nodeResult.getOrNull())
            } else {
                _error.value = nodeResult.exceptionOrNull()?.message ?: "Failed to load node"
            }

            if (commentsResult.isSuccess) {
                _comments.value = commentsResult.getOrNull() ?: emptyList()
            }

            if (childNodesResult.isSuccess) {
                _childNodes.value = childNodesResult.getOrNull() ?: emptyList()
            }

            _isLoading.value = false
        }
    }

    /**
     * リアクションを切り替え
     */
    fun toggleReaction(type: ReactionType) {
        val node = selectedNode.value ?: return
        viewModelScope.launch {
            val result = reactionRepository.toggleReaction(node.id, type)
            if (result.isSuccess) {
                // リアクション成功後にノード詳細を再取得して最新状態を反映
                val nodeResult = nodeRepository.getNode(node.id)
                if (nodeResult.isSuccess) {
                    nodeStore.selectNode(nodeResult.getOrNull())
                }
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to toggle reaction"
            }
        }
    }

    fun updateCommentText(text: String) {
        _commentText.value = text
    }

    /**
     * コメントを投稿
     */
    fun submitComment() {
        val nodeId = selectedNode.value?.id ?: return
        val text = _commentText.value.trim()
        if (text.isEmpty()) return

        viewModelScope.launch {
            _isCommentSubmitting.value = true
            _error.value = null

            val result = commentRepository.createComment(
                nodeId = nodeId,
                content = text
            )

            if (result.isSuccess) {
                _commentText.value = ""
                result.getOrNull()?.let { comment ->
                    _comments.value = _comments.value + comment
                }
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to post comment"
            }

            _isCommentSubmitting.value = false
        }
    }

    /**
     * ノードを選択（子ノードへの遷移等）
     */
    fun selectNode(node: Node) {
        nodeStore.selectNode(node)
    }
}
