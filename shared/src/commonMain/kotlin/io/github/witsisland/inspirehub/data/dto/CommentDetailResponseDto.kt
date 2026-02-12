package io.github.witsisland.inspirehub.data.dto

import kotlinx.serialization.Serializable

/**
 * `GET /comments/{id}` のレスポンスDTO
 *
 * 個別コメントの詳細を返す。
 * APIレスポンス形式: `{ "comment": { ... } }`
 *
 * @property comment コメント詳細
 * @see CommentDto コメントのデータ構造
 */
@Serializable
data class CommentDetailResponseDto(
    val comment: CommentDto
)
