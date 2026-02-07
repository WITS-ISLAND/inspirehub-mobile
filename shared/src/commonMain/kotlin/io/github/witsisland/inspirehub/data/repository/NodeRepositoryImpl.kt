package io.github.witsisland.inspirehub.data.repository

import io.github.witsisland.inspirehub.data.mapper.toDomain
import io.github.witsisland.inspirehub.data.source.NodeDataSource
import io.github.witsisland.inspirehub.domain.model.Node
import io.github.witsisland.inspirehub.domain.model.NodeType
import io.github.witsisland.inspirehub.domain.repository.NodeRepository

class NodeRepositoryImpl(
    private val nodeDataSource: NodeDataSource
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
            val dto = nodeDataSource.createNode(
                title = title,
                content = content,
                type = typeString,
                tags = tags
            )
            Result.success(dto.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleLike(nodeId: String): Result<Node> {
        return try {
            val dto = nodeDataSource.toggleLike(nodeId)
            Result.success(dto.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getChildNodes(parentNodeId: String): Result<List<Node>> {
        return try {
            val dtos = nodeDataSource.getNodes()
            val children = dtos
                .map { it.toDomain() }
                .filter { it.parentNode?.id == parentNodeId }
            Result.success(children)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
