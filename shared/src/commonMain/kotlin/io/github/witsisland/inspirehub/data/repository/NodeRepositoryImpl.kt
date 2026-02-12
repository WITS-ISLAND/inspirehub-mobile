package io.github.witsisland.inspirehub.data.repository

import io.github.witsisland.inspirehub.data.mapper.toDomain
import io.github.witsisland.inspirehub.data.source.NodeDataSource
import io.github.witsisland.inspirehub.data.source.ReactionDataSource
import io.github.witsisland.inspirehub.domain.model.Node
import io.github.witsisland.inspirehub.domain.model.NodeType
import io.github.witsisland.inspirehub.domain.repository.NodeRepository
import kotlin.native.HiddenFromObjC

@HiddenFromObjC
class NodeRepositoryImpl(
    private val nodeDataSource: NodeDataSource,
    private val reactionDataSource: ReactionDataSource
) : NodeRepository {

    override suspend fun getNodes(
        type: String?,
        limit: Int,
        offset: Int
    ): Result<List<Node>> {
        return try {
            val dtos = nodeDataSource.getNodes(type = type, limit = limit, offset = offset)
            Result.success(dtos.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getNode(id: String): Result<Node> {
        return try {
            val dto = nodeDataSource.getNode(id)
            Result.success(dto.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createNode(
        title: String,
        content: String,
        type: NodeType,
        parentNodeId: String?,
        tags: List<String>
    ): Result<Node> {
        return try {
            val typeString = when (type) {
                NodeType.ISSUE -> "issue"
                NodeType.IDEA -> "idea"
                NodeType.PROJECT -> "project"
            }
            // POST /nodes は id のみ返す → getNode で完全なノードを取得
            val nodeId = nodeDataSource.createNode(
                title = title,
                content = content,
                type = typeString,
                tags = tags,
                parentNodeId = parentNodeId
            )
            val dto = nodeDataSource.getNode(nodeId)
            Result.success(dto.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateNode(
        id: String,
        title: String,
        content: String,
        tags: List<String>
    ): Result<Node> {
        return try {
            nodeDataSource.updateNode(
                id = id,
                title = title,
                content = content,
                tags = tags
            )
            // PUT /nodes/{id} は { "message": string } のみ返す → getNode で再取得
            val dto = nodeDataSource.getNode(id)
            Result.success(dto.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteNode(id: String): Result<Unit> {
        return try {
            nodeDataSource.deleteNode(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleLike(nodeId: String): Result<Node> {
        return try {
            // POST /nodes/{id}/like は ReactionSummaryDto を返す
            // 完全なノードデータが必要なので、reaction後に getNode で再取得
            reactionDataSource.toggleLike(nodeId)
            val dto = nodeDataSource.getNode(nodeId)
            Result.success(dto.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getChildNodes(parentNodeId: String): Result<List<Node>> {
        return try {
            val dtos = nodeDataSource.getNodes(limit = 100)
            val children = dtos
                .map { it.toDomain() }
                .filter { it.parentNode?.id == parentNodeId }
            Result.success(children)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchNodes(
        query: String,
        type: String?,
        limit: Int,
        offset: Int
    ): Result<List<Node>> {
        return try {
            val dtos = nodeDataSource.searchNodes(
                query = query,
                type = type,
                limit = limit,
                offset = offset
            )
            Result.success(dtos.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getReactedNodes(
        limit: Int,
        offset: Int
    ): Result<List<Node>> {
        return try {
            val dtos = nodeDataSource.getReactedNodes(limit = limit, offset = offset)
            Result.success(dtos.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
