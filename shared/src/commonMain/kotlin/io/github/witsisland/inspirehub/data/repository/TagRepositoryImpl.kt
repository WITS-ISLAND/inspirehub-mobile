package io.github.witsisland.inspirehub.data.repository

import io.github.witsisland.inspirehub.data.mapper.toDomain
import io.github.witsisland.inspirehub.data.source.TagDataSource
import io.github.witsisland.inspirehub.domain.model.Node
import io.github.witsisland.inspirehub.domain.model.Tag
import io.github.witsisland.inspirehub.domain.repository.TagRepository
import kotlin.native.HiddenFromObjC

@HiddenFromObjC
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

    override suspend fun getNodesByTagName(tagName: String, limit: Int, offset: Int): Result<List<Node>> {
        return try {
            val dtos = tagDataSource.getNodesByTagName(tagName, limit, offset)
            Result.success(dtos.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
