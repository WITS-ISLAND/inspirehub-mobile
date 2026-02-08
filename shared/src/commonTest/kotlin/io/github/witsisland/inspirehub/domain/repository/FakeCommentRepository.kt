package io.github.witsisland.inspirehub.domain.repository

import io.github.witsisland.inspirehub.domain.model.Comment

/**
 * CommentRepositoryのFake実装（テスト用）
 */
class FakeCommentRepository : CommentRepository {

    // テスト用コメントリスト
    var comments: MutableList<Comment> = mutableListOf()

    // 各メソッドの戻り値（nullの場合はcommentsリストから自動生成）
    var getCommentsResult: Result<List<Comment>>? = null
    var createCommentResult: Result<String>? = null
    var updateCommentResult: Result<Unit>? = null
    var deleteCommentResult: Result<Unit>? = null

    // エラーシミュレート用フラグ
    var shouldReturnError: Boolean = false
    var errorMessage: String = "Test error"

    // 呼び出し回数をカウント
    var getCommentsCallCount = 0
    var createCommentCallCount = 0
    var updateCommentCallCount = 0
    var deleteCommentCallCount = 0

    // 最後に渡された引数を保存
    var lastGetCommentsNodeId: String? = null
    var lastCreateCommentNodeId: String? = null
    var lastCreateCommentContent: String? = null
    var lastCreateCommentParentId: String? = null
    var lastUpdateCommentId: String? = null
    var lastUpdateCommentContent: String? = null
    var lastDeleteCommentId: String? = null

    override suspend fun getComments(
        nodeId: String,
        limit: Int,
        offset: Int
    ): Result<List<Comment>> {
        getCommentsCallCount++
        lastGetCommentsNodeId = nodeId

        if (shouldReturnError) return Result.failure(Exception(errorMessage))
        return getCommentsResult
            ?: Result.success(comments.filter { it.nodeId == nodeId })
    }

    override suspend fun createComment(
        nodeId: String,
        content: String,
        parentId: String?
    ): Result<String> {
        createCommentCallCount++
        lastCreateCommentNodeId = nodeId
        lastCreateCommentContent = content
        lastCreateCommentParentId = parentId

        if (shouldReturnError) return Result.failure(Exception(errorMessage))
        return createCommentResult ?: Result.success("comment_new")
    }

    override suspend fun updateComment(id: String, content: String): Result<Unit> {
        updateCommentCallCount++
        lastUpdateCommentId = id
        lastUpdateCommentContent = content

        if (shouldReturnError) return Result.failure(Exception(errorMessage))
        return updateCommentResult ?: Result.success(Unit)
    }

    override suspend fun deleteComment(id: String): Result<Unit> {
        deleteCommentCallCount++
        lastDeleteCommentId = id

        if (shouldReturnError) return Result.failure(Exception(errorMessage))
        return deleteCommentResult ?: Result.success(Unit)
    }

    fun reset() {
        comments.clear()
        getCommentsResult = null
        createCommentResult = null
        updateCommentResult = null
        deleteCommentResult = null
        shouldReturnError = false
        errorMessage = "Test error"
        getCommentsCallCount = 0
        createCommentCallCount = 0
        updateCommentCallCount = 0
        deleteCommentCallCount = 0
        lastGetCommentsNodeId = null
        lastCreateCommentNodeId = null
        lastCreateCommentContent = null
        lastCreateCommentParentId = null
        lastUpdateCommentId = null
        lastUpdateCommentContent = null
        lastDeleteCommentId = null
    }
}
