package io.github.witsisland.inspirehub.data.source

import io.github.witsisland.inspirehub.data.dto.NodeDto

/**
 * ノードデータソースインターフェース
 */
interface NodeDataSource {
    /**
     * ノード一覧を取得
     * @param type フィルタ: "idea" | "issue" | "project" | null
     * @param limit 取得件数（最大100）
     * @param offset オフセット
     */
    suspend fun getNodes(
        type: String? = null,
        limit: Int = 20,
        offset: Int = 0
    ): List<NodeDto>

    /**
     * ノード詳細を取得
     * @param id ノードID
     */
    suspend fun getNode(id: String): NodeDto

    /**
     * ノードを作成
     * @param title タイトル
     * @param content 本文
     * @param type "idea" | "issue" | "project"
     * @param tags タグID一覧
     */
    suspend fun createNode(
        title: String,
        content: String,
        type: String,
        tags: List<String> = emptyList()
    ): NodeDto

    /**
     * ノードを更新
     * @param id ノードID
     * @param title タイトル
     * @param content 本文
     * @param tags タグID一覧
     */
    suspend fun updateNode(
        id: String,
        title: String,
        content: String,
        tags: List<String> = emptyList()
    ): NodeDto

    /**
     * ノードを削除
     * @param id ノードID
     */
    suspend fun deleteNode(id: String)

    /**
     * ノードにいいねを切り替え
     * @param id ノードID
     */
    suspend fun toggleLike(id: String): NodeDto

    /**
     * ノードをキーワード検索
     * @param query 検索キーワード
     * @param type フィルタ: "idea" | "issue" | "project" | null
     * @param limit 取得件数（最大100）
     * @param offset オフセット
     */
    suspend fun searchNodes(
        query: String,
        type: String? = null,
        limit: Int = 20,
        offset: Int = 0
    ): List<NodeDto>
}
