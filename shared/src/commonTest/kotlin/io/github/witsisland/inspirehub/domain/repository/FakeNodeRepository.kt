package io.github.witsisland.inspirehub.domain.repository

import io.github.witsisland.inspirehub.domain.model.Node
import io.github.witsisland.inspirehub.domain.model.NodeType

class FakeNodeRepository : NodeRepository {

    var nodes: MutableList<Node> = mutableListOf()

    var getNodesResult: Result<List<Node>>? = null
    var getNodeResult: Result<Node>? = null
    var createNodeResult: Result<Node>? = null
    var toggleLikeResult: Result<Node>? = null
    var getChildNodesResult: Result<List<Node>>? = null

    var shouldReturnError: Boolean = false
    var errorMessage: String = "Test error"

    var getNodesCallCount = 0
    var getNodeCallCount = 0
    var createNodeCallCount = 0
    var toggleLikeCallCount = 0
    var getChildNodesCallCount = 0

    var lastGetNodesType: String? = null
    var lastGetNodesLimit: Int? = null
    var lastGetNodesOffset: Int? = null
    var lastGetNodeId: String? = null
    var lastCreateNodeTitle: String? = null
    var lastCreateNodeContent: String? = null
    var lastCreateNodeType: NodeType? = null
    var lastCreateNodeParentNodeId: String? = null
    var lastCreateNodeTags: List<String>? = null
    var lastToggleLikeNodeId: String? = null
    var lastGetChildNodesParentNodeId: String? = null

    override suspend fun getNodes(
        type: String?,
        limit: Int,
        offset: Int
    ): Result<List<Node>> {
        getNodesCallCount++
        lastGetNodesType = type
        lastGetNodesLimit = limit
        lastGetNodesOffset = offset

        if (shouldReturnError) return Result.failure(Exception(errorMessage))
        return getNodesResult ?: Result.success(nodes.toList())
    }

    override suspend fun getNode(id: String): Result<Node> {
        getNodeCallCount++
        lastGetNodeId = id

        if (shouldReturnError) return Result.failure(Exception(errorMessage))
        return getNodeResult
            ?: nodes.find { it.id == id }?.let { Result.success(it) }
            ?: Result.failure(Exception("Node not found: $id"))
    }

    override suspend fun createNode(
        title: String,
        content: String,
        type: NodeType,
        parentNodeId: String?,
        tags: List<String>
    ): Result<Node> {
        createNodeCallCount++
        lastCreateNodeTitle = title
        lastCreateNodeContent = content
        lastCreateNodeType = type
        lastCreateNodeParentNodeId = parentNodeId
        lastCreateNodeTags = tags

        if (shouldReturnError) return Result.failure(Exception(errorMessage))
        return createNodeResult ?: error("createNodeResult not set")
    }

    override suspend fun toggleLike(nodeId: String): Result<Node> {
        toggleLikeCallCount++
        lastToggleLikeNodeId = nodeId

        if (shouldReturnError) return Result.failure(Exception(errorMessage))
        return toggleLikeResult ?: error("toggleLikeResult not set")
    }

    override suspend fun getChildNodes(parentNodeId: String): Result<List<Node>> {
        getChildNodesCallCount++
        lastGetChildNodesParentNodeId = parentNodeId

        if (shouldReturnError) return Result.failure(Exception(errorMessage))
        return getChildNodesResult
            ?: Result.success(nodes.filter { it.parentNode?.id == parentNodeId })
    }

    fun reset() {
        nodes.clear()
        getNodesResult = null
        getNodeResult = null
        createNodeResult = null
        toggleLikeResult = null
        getChildNodesResult = null
        shouldReturnError = false
        errorMessage = "Test error"
        getNodesCallCount = 0
        getNodeCallCount = 0
        createNodeCallCount = 0
        toggleLikeCallCount = 0
        getChildNodesCallCount = 0
        lastGetNodesType = null
        lastGetNodesLimit = null
        lastGetNodesOffset = null
        lastGetNodeId = null
        lastCreateNodeTitle = null
        lastCreateNodeContent = null
        lastCreateNodeType = null
        lastCreateNodeParentNodeId = null
        lastCreateNodeTags = null
        lastToggleLikeNodeId = null
        lastGetChildNodesParentNodeId = null
    }
}
