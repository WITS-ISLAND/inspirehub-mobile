package io.github.witsisland.inspirehub.presentation.viewmodel

import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import com.rickclephas.kmp.observableviewmodel.MutableStateFlow
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import io.github.witsisland.inspirehub.domain.model.Node
import io.github.witsisland.inspirehub.domain.model.Tag
import io.github.witsisland.inspirehub.domain.repository.NodeRepository
import io.github.witsisland.inspirehub.domain.repository.TagRepository
import io.github.witsisland.inspirehub.domain.store.DiscoverStore
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DiscoverViewModel(
    private val discoverStore: DiscoverStore,
    private val nodeRepository: NodeRepository,
    private val tagRepository: TagRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow(viewModelScope, "")
    @NativeCoroutinesState
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Node>>(viewModelScope, emptyList())
    @NativeCoroutinesState
    val searchResults: StateFlow<List<Node>> = _searchResults.asStateFlow()

    private val _popularTags = MutableStateFlow<List<Tag>>(viewModelScope, emptyList())
    @NativeCoroutinesState
    val popularTags: StateFlow<List<Tag>> = _popularTags.asStateFlow()

    private val _popularNodes = MutableStateFlow<List<Node>>(viewModelScope, emptyList())
    @NativeCoroutinesState
    val popularNodes: StateFlow<List<Node>> = _popularNodes.asStateFlow()

    private val _selectedTag = MutableStateFlow<Tag?>(viewModelScope, null)
    @NativeCoroutinesState
    val selectedTag: StateFlow<Tag?> = _selectedTag.asStateFlow()

    private val _tagNodes = MutableStateFlow<List<Node>>(viewModelScope, emptyList())
    @NativeCoroutinesState
    val tagNodes: StateFlow<List<Node>> = _tagNodes.asStateFlow()

    private val _isLoading = MutableStateFlow(viewModelScope, false)
    @NativeCoroutinesState
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow(viewModelScope, null as String?)
    @NativeCoroutinesState
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        viewModelScope.launch {
            discoverStore.searchResults.collect { _searchResults.value = it }
        }
        viewModelScope.launch {
            discoverStore.popularTags.collect { _popularTags.value = it }
        }
        viewModelScope.launch {
            discoverStore.popularNodes.collect { _popularNodes.value = it }
        }
        viewModelScope.launch {
            discoverStore.isLoading.collect { _isLoading.value = it }
        }
        viewModelScope.launch {
            discoverStore.searchQuery.collect { _searchQuery.value = it }
        }
        viewModelScope.launch {
            discoverStore.selectedTag.collect { _selectedTag.value = it }
        }
        viewModelScope.launch {
            discoverStore.tagNodes.collect { _tagNodes.value = it }
        }
    }

    fun search(query: String) {
        discoverStore.setSearchQuery(query)
        if (query.isBlank()) {
            discoverStore.updateSearchResults(emptyList())
            return
        }
        // テキスト検索時はタグフィルタを解除
        discoverStore.setSelectedTag(null)
        discoverStore.updateTagNodes(emptyList())

        viewModelScope.launch {
            discoverStore.setLoading(true)
            _error.value = null

            val result = nodeRepository.searchNodes(query = query)
            if (result.isSuccess) {
                discoverStore.updateSearchResults(result.getOrThrow())
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Search failed"
            }

            discoverStore.setLoading(false)
        }
    }

    fun loadPopularTags() {
        viewModelScope.launch {
            val result = tagRepository.getPopularTags()
            if (result.isSuccess) {
                discoverStore.updatePopularTags(result.getOrThrow())
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to load popular tags"
            }
        }
    }

    fun loadPopularNodes() {
        viewModelScope.launch {
            discoverStore.setLoading(true)
            _error.value = null

            val result = nodeRepository.getNodes(limit = 10)
            if (result.isSuccess) {
                val sorted = result.getOrThrow().sortedByDescending { node ->
                    node.reactions.like.count +
                        node.reactions.interested.count +
                        node.reactions.wantToTry.count
                }
                discoverStore.updatePopularNodes(sorted)
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to load popular nodes"
            }

            discoverStore.setLoading(false)
        }
    }

    /**
     * タグを選択してそのタグのノード一覧を取得
     */
    fun selectTag(tag: Tag) {
        // 同じタグをタップしたら解除
        if (_selectedTag.value?.id == tag.id) {
            clearTagFilter()
            return
        }

        discoverStore.setSelectedTag(tag)
        discoverStore.setSearchQuery("")
        discoverStore.updateSearchResults(emptyList())

        viewModelScope.launch {
            discoverStore.setLoading(true)
            _error.value = null

            val result = tagRepository.getNodesByTagName(tag.name)
            if (result.isSuccess) {
                discoverStore.updateTagNodes(result.getOrThrow())
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to load tag nodes"
            }

            discoverStore.setLoading(false)
        }
    }

    /**
     * タグフィルタを解除
     */
    fun clearTagFilter() {
        discoverStore.setSelectedTag(null)
        discoverStore.updateTagNodes(emptyList())
    }
}
