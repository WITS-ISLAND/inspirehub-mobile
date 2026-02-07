package io.github.witsisland.inspirehub.data.source

import io.github.witsisland.inspirehub.data.dto.NodeDto

/**
 * ノードデータソースインターフェース
 *
 * API: /nodes
 */
interface NodeDataSource {
    /**
     * GET /nodes
     * Response: { "nodes": [NodeDto], "total": number }
     *
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
     * GET /nodes/{id}
     * Response: NodeDto（直接）
     *
     * @param id ノードID
     */
    suspend fun getNode(id: String): NodeDto

    /**
     * POST /nodes
     * Request: CreateNodeRequestDto
     * Response: { "id": string, "message": string } (201 Created)
     *
     * @param title タイトル
     * @param content 本文
     * @param type "idea" | "issue" | "project"
     * @param tags タグID一覧
     * @param parentNodeId 派生元ノードID（派生投稿の場合）
     * @return 作成されたノードのID
     */
    suspend fun createNode(
        title: String,
        content: String,
        type: String,
        tags: List<String> = emptyList(),
        parentNodeId: String? = null
    ): String

    /**
     * PUT /nodes/{id}
     * Request: UpdateNodeRequestDto
     * Response: NodeDto
     *
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
     * DELETE /nodes/{id}
     * Response: 204 No Content
     *
     * @param id ノードID
     */
    suspend fun deleteNode(id: String)

    /**
     * GET /nodes?q={query}
     * Response: { "nodes": [NodeDto], "total": number }
     *
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

    /**
     * リアクション済みノード一覧を取得
     * @param limit 取得件数
     * @param offset オフセット
     */
    suspend fun getReactedNodes(
        limit: Int = 20,
        offset: Int = 0
    ): List<NodeDto>
}
