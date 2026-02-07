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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    }

    private var searchJob: Job? = null

    fun search(query: String) {
        searchJob?.cancel()
        discoverStore.setSearchQuery(query)
        if (query.isBlank()) {
            discoverStore.setLoading(false)
            discoverStore.updateSearchResults(emptyList())
            return
        }
        discoverStore.setLoading(true)
        _error.value = null
        searchJob = viewModelScope.launch {
            delay(300) // debounce: 入力中はAPI呼び出しを抑制
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

    fun selectTag(tag: Tag) {
        search(tag.name)
    }
}
