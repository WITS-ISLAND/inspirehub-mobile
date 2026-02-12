package io.github.witsisland.inspirehub.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * `POST /nodes/{nodeId}/comments` のリクエストボディDTO
 *
 * ノードに対する新規コメントまたは返信を作成する。Bearer認証が必要。
 *
 * 注意: API仕様には `mentions` フィールド（`List<String>`）が存在するが、
 * Phase 1では未実装のため省略している。
 *
 * @property content コメント本文（1文字以上必須）
 * @property parentId 返信先コメントのID（トップレベルコメントの場合null）
 */
@Serializable
data class CreateCommentRequestDto(
    val content: String,
    @SerialName("parent_id")
    val parentId: String? = null
)

/**
 * `PUT /comments/{id}` のリクエストボディDTO
 *
 * 既存コメントの内容を更新する。Bearer認証が必要（投稿者本人のみ）。
 *
 * 注意: API仕様には `mentions` フィールド（`List<String>`）が存在するが、
 * Phase 1では未実装のため省略している。
 *
 * @property content 更新後のコメント本文（1文字以上必須）
 */
@Serializable
data class UpdateCommentRequestDto(
    val content: String
)
