package io.github.witsisland.inspirehub.data.dto

import kotlinx.serialization.Serializable

/**
 * `POST /nodes/{nodeId}/comments` のレスポンスDTO (HTTP 201 Created)
 *
 * コメント作成成功時に返却される。完全なコメントデータは含まれない。
 *
 * @property id 作成されたコメントの一意識別子
 * @property message 作成成功メッセージ
 */
@Serializable
data class CreateCommentResponseDto(
    val id: String,
    val message: String
)
