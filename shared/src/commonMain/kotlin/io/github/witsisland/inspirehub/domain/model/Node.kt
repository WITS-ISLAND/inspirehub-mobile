package io.github.witsisland.inspirehub.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * ノード（課題/アイデアの投稿）
 * アプリ全体の中心となるエンティティ
 */
@Serializable
data class Node(
    val id: String,
    val type: NodeType,
    val title: String,
    val content: String,
    val authorId: String,
    val parentNodeId: String? = null, // 派生元ノード（派生アイデアの場合に設定）
    val tagIds: List<String> = emptyList(), // タグID一覧（Phase 2〜）
    val likeCount: Int = 0,
    val isLiked: Boolean = false,
    val commentCount: Int = 0,
    val createdAt: Instant,
    val updatedAt: Instant
)
