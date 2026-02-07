package io.github.witsisland.inspirehub.data.source

import io.github.witsisland.inspirehub.data.dto.CreateNodeRequestDto
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

/**
 * Ktor Client を使用した NodeDataSource 実装
 */
class KtorNodeDataSource(
    private val httpClient: HttpClient
) : NodeDataSource {

    override suspend fun getNodes(
        type: String?,
        limit: Int,
        offset: Int
    ): List<NodeDto> {
        val response: NodesResponseDto = httpClient.get("/nodes") {
            parameter("limit", limit)
            parameter("offset", offset)
            type?.let { parameter("type", it) }
        }.body()
        return response.nodes
    }

    override suspend fun getNode(id: String): NodeDto {
        return httpClient.get("/nodes/$id").body()
    }

    override suspend fun createNode(
        title: String,
        content: String,
        type: String,
        tags: List<String>
    ): NodeDto {
        return httpClient.post("/nodes") {
            contentType(ContentType.Application.Json)
            setBody(CreateNodeRequestDto(
                title = title,
                content = content,
                type = type,
                tags = tags
            ))
        }.body()
    }

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

    override suspend fun deleteNode(id: String) {
        httpClient.delete("/nodes/$id")
    }

    override suspend fun toggleLike(id: String): NodeDto {
        return httpClient.post("/nodes/$id/like").body()
    }

    override suspend fun searchNodes(
        query: String,
        type: String?,
        limit: Int,
        offset: Int
    ): List<NodeDto> {
        val response: NodesResponseDto = httpClient.get("/nodes") {
            parameter("q", query)
            parameter("limit", limit)
            parameter("offset", offset)
            type?.let { parameter("type", it) }
        }.body()
        return response.nodes
    }
}
