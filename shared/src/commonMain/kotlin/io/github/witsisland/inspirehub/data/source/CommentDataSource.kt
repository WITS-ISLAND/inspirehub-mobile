package io.github.witsisland.inspirehub.data.source

import io.github.witsisland.inspirehub.data.dto.CommentDto

/**
 * コメントデータソースインターフェース
 */
interface CommentDataSource {
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
    ): List<CommentDto>

    /**
     * コメント詳細を取得
     * @param id コメントID
     */
    suspend fun getComment(id: String): CommentDto

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
    ): CommentDto

    /**
     * コメントを更新
     * @param id コメントID
     * @param content コメント内容
     */
    suspend fun updateComment(
        id: String,
        content: String
    ): CommentDto

    /**
     * コメントを削除
     * @param id コメントID
     */
    suspend fun deleteComment(id: String)
}
