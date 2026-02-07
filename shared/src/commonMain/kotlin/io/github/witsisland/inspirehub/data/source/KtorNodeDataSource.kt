package io.github.witsisland.inspirehub.data.source

import io.github.witsisland.inspirehub.data.dto.CreateNodeRequestDto
import io.github.witsisland.inspirehub.data.dto.CreateNodeResponseDto
import io.github.witsisland.inspirehub.data.dto.NodeDto
import io.github.witsisland.inspirehub.data.dto.NodesResponseDto
import io.github.witsisland.inspirehub.data.dto.UpdateNodeRequestDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import co.touchlab.kermit.Logger as KermitLogger

/**
 * Ktor Client を使用した NodeDataSource 実装
 *
 * API: /nodes
 */
class KtorNodeDataSource(
    private val httpClient: HttpClient
) : NodeDataSource {

    private val log = KermitLogger.withTag("KtorNodeDataSource")

    /**
     * GET /nodes
     * Response: { "nodes": [NodeDto], "total": number }
     */
    override suspend fun getNodes(
        type: String?,
        limit: Int,
        offset: Int
    ): List<NodeDto> {
        val response: NodesResponseDto = httpClient.get("/nodes") {
            type?.let { parameter("type", it) }
            parameter("limit", limit)
            parameter("offset", offset)
        }.body()
        return response.nodes
    }

    /**
     * GET /nodes/{id}
     * Response: NodeDto（直接）
     */
    override suspend fun getNode(id: String): NodeDto {
        return httpClient.get("/nodes/$id").body()
    }

    /**
     * POST /nodes
     * Request: CreateNodeRequestDto
     * Response: { "id": string, "message": string } (201 Created)
     */
    override suspend fun createNode(
        title: String,
        content: String,
        type: String,
        tags: List<String>,
        parentNodeId: String?
    ): String {
        log.d { "createNode: parentNodeId=$parentNodeId" }
        val response: CreateNodeResponseDto = httpClient.post("/nodes") {
            contentType(ContentType.Application.Json)
            setBody(CreateNodeRequestDto(
                title = title,
                content = content,
                type = type,
                tags = tags,
                parentNodeId = parentNodeId
            ))
        }.body()
        return response.id
    }

    /**
     * PUT /nodes/{id}
     * Request: UpdateNodeRequestDto
     * Response: NodeDto
     */
    override suspend fun updateNode(
        id: String,
        title: String,
        content: String,
        tags: List<String>
    ): NodeDto {
        return httpClient.put("/nodes/$id") {
            contentType(ContentType.Application.Json)
            setBody(UpdateNodeRequestDto(
                title = title,
                content = content,
                tags = tags
            ))
        }.body()
    }

    /**
     * DELETE /nodes/{id}
     * Response: 204 No Content
     */
    override suspend fun deleteNode(id: String) {
        httpClient.delete("/nodes/$id")
    }

    /**
     * GET /nodes?q={query}
     * Response: { "nodes": [NodeDto], "total": number }
     */
    override suspend fun searchNodes(
        query: String,
        type: String?,
        limit: Int,
        offset: Int
    ): List<NodeDto> {
        val response: NodesResponseDto = httpClient.get("/nodes") {
            parameter("q", query)
            type?.let { parameter("type", it) }
            parameter("limit", limit)
            parameter("offset", offset)
        }.body()
        return response.nodes
    }
}
