package io.github.witsisland.inspirehub.domain.store

import io.github.witsisland.inspirehub.domain.model.Node
import io.github.witsisland.inspirehub.domain.model.Tag
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.native.HiddenFromObjC

@HiddenFromObjC
class DiscoverStore {
    private val _searchResults = MutableStateFlow<List<Node>>(emptyList())
    val searchResults: StateFlow<List<Node>> = _searchResults.asStateFlow()

    private val _popularTags = MutableStateFlow<List<Tag>>(emptyList())
    val popularTags: StateFlow<List<Tag>> = _popularTags.asStateFlow()

    private val _popularNodes = MutableStateFlow<List<Node>>(emptyList())
    val popularNodes: StateFlow<List<Node>> = _popularNodes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTag = MutableStateFlow<Tag?>(null)
    val selectedTag: StateFlow<Tag?> = _selectedTag.asStateFlow()

    private val _tagNodes = MutableStateFlow<List<Node>>(emptyList())
    val tagNodes: StateFlow<List<Node>> = _tagNodes.asStateFlow()

    fun updateSearchResults(nodes: List<Node>) {
        _searchResults.value = nodes
    }

    fun updatePopularTags(tags: List<Tag>) {
        _popularTags.value = tags
    }

    fun updatePopularNodes(nodes: List<Node>) {
        _popularNodes.value = nodes
    }

    fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedTag(tag: Tag?) {
        _selectedTag.value = tag
    }

    fun updateTagNodes(nodes: List<Node>) {
        _tagNodes.value = nodes
    }

    fun clear() {
        _searchResults.value = emptyList()
        _popularTags.value = emptyList()
        _popularNodes.value = emptyList()
        _isLoading.value = false
        _searchQuery.value = ""
        _selectedTag.value = null
        _tagNodes.value = emptyList()
    }
}
