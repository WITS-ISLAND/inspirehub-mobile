package io.github.witsisland.inspirehub.data.dto

import kotlinx.serialization.Serializable

/**
 * `GET /nodes/{nodeId}/comments` のレスポンスDTO
 *
 * 指定ノードに紐づくコメント一覧をページネーション付きで返す。
 * クエリパラメータ: limit, offset
 *
 * @property comments コメント一覧（ネストされた [CommentDto.replies] を含む場合がある）
 * @property total コメントの総数（ページネーション用）
 * @see CommentDto 個別コメントのデータ構造
 */
@Serializable
data class CommentsResponseDto(
    val comments: List<CommentDto>,
    val total: Int
)
