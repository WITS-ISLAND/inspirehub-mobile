package io.github.witsisland.inspirehub.data.source

import io.github.witsisland.inspirehub.data.dto.CommentDetailResponseDto
import io.github.witsisland.inspirehub.data.dto.CommentDto
import io.github.witsisland.inspirehub.data.dto.CommentsResponseDto
import io.github.witsisland.inspirehub.data.dto.CreateCommentRequestDto
import io.github.witsisland.inspirehub.data.dto.CreateCommentResponseDto
import io.github.witsisland.inspirehub.data.dto.UpdateCommentRequestDto
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
 * Ktor Client を使用した CommentDataSource 実装
 *
 * API: /nodes/{nodeId}/comments, /comments/{id}
 */
class KtorCommentDataSource(
    private val httpClient: HttpClient
) : CommentDataSource {

    /**
     * GET /nodes/{nodeId}/comments
     * Response: { "comments": [CommentDto], "total": number }
     */
    override suspend fun getComments(
        nodeId: String,
        limit: Int,
        offset: Int
    ): List<CommentDto> {
        val response: CommentsResponseDto = httpClient.get("/nodes/$nodeId/comments") {
            parameter("limit", limit)
            parameter("offset", offset)
        }.body()
        return response.comments
    }

    /**
     * GET /comments/{id}
     * Response: { "comment": CommentDto }
     */
    override suspend fun getComment(id: String): CommentDto {
        val response: CommentDetailResponseDto = httpClient.get("/comments/$id").body()
        return response.comment
    }

    /**
     * POST /nodes/{nodeId}/comments
     * Request: CreateCommentRequestDto
     * Response: { "id": string, "message": string } (201 Created)
     */
    override suspend fun createComment(
        nodeId: String,
        content: String,
        parentId: String?
    ): String {
        val response: CreateCommentResponseDto = httpClient.post("/nodes/$nodeId/comments") {
            contentType(ContentType.Application.Json)
            setBody(CreateCommentRequestDto(
                content = content,
                parentId = parentId
            ))
        }.body()
        return response.id
    }

    /**
     * PUT /comments/{id}
     * Request: UpdateCommentRequestDto
     * Response: { "message": string }
     */
    override suspend fun updateComment(
        id: String,
        content: String
    ) {
        httpClient.put("/comments/$id") {
            contentType(ContentType.Application.Json)
            setBody(UpdateCommentRequestDto(content = content))
        }
    }

    /**
     * DELETE /comments/{id}
     * Response: 204 No Content
     */
    override suspend fun deleteComment(id: String) {
        httpClient.delete("/comments/$id")
    }
}
