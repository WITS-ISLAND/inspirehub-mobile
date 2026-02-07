package io.github.witsisland.inspirehub.data.source

import io.github.witsisland.inspirehub.data.dto.NodeDto
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

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
        return httpClient.get("/nodes") {
            parameter("limit", limit)
            parameter("offset", offset)
            type?.let { parameter("type", it) }
        }.body()
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
            setBody(mapOf(
                "title" to title,
                "content" to content,
                "type" to type,
                "tags" to tags
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
            setBody(mapOf(
                "title" to title,
                "content" to content,
                "tags" to tags
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
        return httpClient.get("/nodes") {
            parameter("q", query)
            parameter("limit", limit)
            parameter("offset", offset)
            type?.let { parameter("type", it) }
        }.body()
    }
}
