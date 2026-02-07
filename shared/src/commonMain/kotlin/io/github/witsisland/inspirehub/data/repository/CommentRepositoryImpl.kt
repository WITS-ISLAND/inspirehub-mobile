package io.github.witsisland.inspirehub.data.repository

import io.github.witsisland.inspirehub.data.mapper.toDomain
import io.github.witsisland.inspirehub.data.source.CommentDataSource
import io.github.witsisland.inspirehub.domain.model.Comment
import io.github.witsisland.inspirehub.domain.repository.CommentRepository

/**
 * CommentRepository の実装
 * CommentDataSource を通じてデータ取得し、ドメインモデルに変換する
 */
class CommentRepositoryImpl(
    private val dataSource: CommentDataSource
) : CommentRepository {

    override suspend fun getComments(
        nodeId: String,
        limit: Int,
        offset: Int
    ): Result<List<Comment>> {
        return try {
            val dtos = dataSource.getComments(nodeId, limit = limit, offset = offset)
            Result.success(dtos.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createComment(
        nodeId: String,
        content: String,
        parentId: String?
    ): Result<String> {
        return try {
            val commentId = dataSource.createComment(nodeId, content, parentId)
            Result.success(commentId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteComment(id: String): Result<Unit> {
        return try {
            dataSource.deleteComment(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
