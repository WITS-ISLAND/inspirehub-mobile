package io.github.witsisland.inspirehub.presentation.viewmodel

import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import com.rickclephas.kmp.observableviewmodel.MutableStateFlow
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import io.github.witsisland.inspirehub.domain.model.Node
import io.github.witsisland.inspirehub.domain.model.NodeType
import io.github.witsisland.inspirehub.domain.model.Tag
import io.github.witsisland.inspirehub.domain.repository.NodeRepository
import io.github.witsisland.inspirehub.domain.repository.TagRepository
import io.github.witsisland.inspirehub.domain.store.NodeStore
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 投稿ViewModel
 * 課題(ISSUE)・アイデア(IDEA)・派生投稿を1つのViewModelで扱う
 */
class PostViewModel(
    private val nodeStore: NodeStore,
    private val nodeRepository: NodeRepository,
    private val tagRepository: TagRepository
) : ViewModel() {

    private val _title = MutableStateFlow(viewModelScope, "")
    @NativeCoroutinesState
    val title: StateFlow<String> = _title.asStateFlow()

    private val _content = MutableStateFlow(viewModelScope, "")
    @NativeCoroutinesState
    val content: StateFlow<String> = _content.asStateFlow()

    private val _tags = MutableStateFlow(viewModelScope, emptyList<String>())
    @NativeCoroutinesState
    val tags: StateFlow<List<String>> = _tags.asStateFlow()

    private val _parentNode = MutableStateFlow(viewModelScope, null as Node?)
    @NativeCoroutinesState
    val parentNode: StateFlow<Node?> = _parentNode.asStateFlow()

    private val _isSubmitting = MutableStateFlow(viewModelScope, false)
    @NativeCoroutinesState
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _error = MutableStateFlow(viewModelScope, null as String?)
    @NativeCoroutinesState
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isSuccess = MutableStateFlow(viewModelScope, false)
    @NativeCoroutinesState
    val isSuccess: StateFlow<Boolean> = _isSuccess.asStateFlow()

    /**
     * フォームが有効かどうか (BUG-010: タイトルと本文の両方が必須)
     */
    private val _isValid = MutableStateFlow(viewModelScope, false)
    @NativeCoroutinesState
    val isValid: StateFlow<Boolean> = _isValid.asStateFlow()

    private val _suggestedTags = MutableStateFlow<List<Tag>>(viewModelScope, emptyList())
    @NativeCoroutinesState
    val suggestedTags: StateFlow<List<Tag>> = _suggestedTags.asStateFlow()

    private var tagSuggestJob: Job? = null

    private fun updateValidation() {
        _isValid.value = _title.value.isNotBlank() && _content.value.isNotBlank()
    }

    fun updateTitle(value: String) {
        _title.value = value
        updateValidation()
    }

    fun updateContent(value: String) {
        _content.value = value
        updateValidation()
    }

    fun addTag(tag: String) {
        if (tag !in _tags.value) {
            _tags.value = _tags.value + tag
        }
    }

    fun removeTag(tag: String) {
        _tags.value = _tags.value - tag
    }

    /**
     * タグサジェストを検索
     */
    fun searchTagSuggestions(query: String) {
        if (query.isBlank()) {
            _suggestedTags.value = emptyList()
            return
        }
        tagSuggestJob?.cancel()
        tagSuggestJob = viewModelScope.launch {
            val result = tagRepository.suggestTags(query)
            if (result.isSuccess) {
                _suggestedTags.value = result.getOrThrow()
            }
        }
    }

    /**
     * タグサジェストをクリア
     */
    fun clearTagSuggestions() {
        _suggestedTags.value = emptyList()
    }

    fun setParentNode(node: Node?) {
        _parentNode.value = node
    }

    /**
     * 課題を投稿
     */
    fun submitIssue() {
        submit(NodeType.ISSUE, parentNodeId = null)
    }

    /**
     * アイデアを投稿
     */
    fun submitIdea() {
        submit(NodeType.IDEA, parentNodeId = null)
    }

    /**
     * 派生投稿（親ノードに紐づくアイデア）
     */
    fun submitDerived() {
        submit(NodeType.IDEA, parentNodeId = _parentNode.value?.id)
    }

    /**
     * フォームをリセット
     */
    fun reset() {
        _title.value = ""
        _content.value = ""
        _tags.value = emptyList()
        _parentNode.value = null
        _isSubmitting.value = false
        _error.value = null
        _isSuccess.value = false
        _isValid.value = false
        _suggestedTags.value = emptyList()
        tagSuggestJob?.cancel()
    }

    private fun submit(type: NodeType, parentNodeId: String?) {
        viewModelScope.launch {
            _isSubmitting.value = true
            _error.value = null
            _isSuccess.value = false

            val result = nodeRepository.createNode(
                title = _title.value,
                content = _content.value,
                type = type,
                parentNodeId = parentNodeId,
                tags = _tags.value
            )

            if (result.isSuccess) {
                result.getOrNull()?.let { node ->
                    nodeStore.addNode(node)
                }
                _isSuccess.value = true
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to create post"
            }

            _isSubmitting.value = false
        }
    }
}
