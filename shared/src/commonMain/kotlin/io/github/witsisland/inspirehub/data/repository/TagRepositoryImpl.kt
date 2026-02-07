package io.github.witsisland.inspirehub.data.repository

import io.github.witsisland.inspirehub.data.mapper.toDomain
import io.github.witsisland.inspirehub.data.source.TagDataSource
import io.github.witsisland.inspirehub.domain.model.Tag
import io.github.witsisland.inspirehub.domain.repository.TagRepository

class TagRepositoryImpl(
    private val tagDataSource: TagDataSource
) : TagRepository {

    override suspend fun getPopularTags(limit: Int): Result<List<Tag>> {
        return try {
            val dtos = tagDataSource.getPopularTags(limit)
            Result.success(dtos.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun suggestTags(query: String, limit: Int): Result<List<Tag>> {
        return try {
            val dtos = tagDataSource.suggestTags(query, limit)
            Result.success(dtos.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
