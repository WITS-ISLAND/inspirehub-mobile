package io.github.witsisland.inspirehub.domain.repository

import io.github.witsisland.inspirehub.domain.model.Node
import io.github.witsisland.inspirehub.domain.model.NodeType

/**
 * ノードリポジトリ
 */
interface NodeRepository {
    suspend fun getNodes(
        type: String? = null,
        limit: Int = 20,
        offset: Int = 0
    ): Result<List<Node>>

    suspend fun getNode(id: String): Result<Node>

    suspend fun createNode(
        title: String,
        content: String,
        type: NodeType,
        parentNodeId: String? = null,
        tags: List<String> = emptyList()
    ): Result<Node>

    suspend fun toggleLike(nodeId: String): Result<Node>

    suspend fun getChildNodes(parentNodeId: String): Result<List<Node>>

    suspend fun searchNodes(
        query: String,
        type: String? = null,
        limit: Int = 20,
        offset: Int = 0
    ): Result<List<Node>>
}
