package io.github.witsisland.inspirehub.domain.repository

import io.github.witsisland.inspirehub.domain.model.Tag

class FakeTagRepository : TagRepository {

    var getPopularTagsResult: Result<List<Tag>>? = null
    var suggestTagsResult: Result<List<Tag>>? = null

    var shouldReturnError: Boolean = false
    var errorMessage: String = "Test error"

    var getPopularTagsCallCount = 0
    var suggestTagsCallCount = 0

    var lastGetPopularTagsLimit: Int? = null
    var lastSuggestTagsQuery: String? = null
    var lastSuggestTagsLimit: Int? = null

    override suspend fun getPopularTags(limit: Int): Result<List<Tag>> {
        getPopularTagsCallCount++
        lastGetPopularTagsLimit = limit

        if (shouldReturnError) return Result.failure(Exception(errorMessage))
        return getPopularTagsResult ?: error("getPopularTagsResult not set")
    }

    override suspend fun suggestTags(query: String, limit: Int): Result<List<Tag>> {
        suggestTagsCallCount++
        lastSuggestTagsQuery = query
        lastSuggestTagsLimit = limit

        if (shouldReturnError) return Result.failure(Exception(errorMessage))
        return suggestTagsResult ?: error("suggestTagsResult not set")
    }

    fun reset() {
        getPopularTagsResult = null
        suggestTagsResult = null
        shouldReturnError = false
        errorMessage = "Test error"
        getPopularTagsCallCount = 0
        suggestTagsCallCount = 0
        lastGetPopularTagsLimit = null
        lastSuggestTagsQuery = null
        lastSuggestTagsLimit = null
    }
}
