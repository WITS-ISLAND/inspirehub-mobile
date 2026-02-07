package io.github.witsisland.inspirehub.domain.repository

import io.github.witsisland.inspirehub.domain.model.Comment

/**
 * コメントリポジトリ
 * ノードに紐づくコメントのCRUD操作を提供
 */
interface CommentRepository {
    /**
     * ノードのコメント一覧を取得
     * @param nodeId ノードID
     * @param limit 取得件数（最大100）
     * @param offset オフセット
     */
    suspend fun getComments(
        nodeId: String,
        limit: Int = 20,
        offset: Int = 0
    ): Result<List<Comment>>

    /**
     * コメントを作成
     * @param nodeId ノードID
     * @param content コメント内容
     * @param parentId 返信先コメントID（nullの場合は新規コメント）
     */
    suspend fun createComment(
        nodeId: String,
        content: String,
        parentId: String? = null
    ): Result<Comment>

    /**
     * コメントを削除
     * @param id コメントID
     */
    suspend fun deleteComment(id: String): Result<Unit>
}
