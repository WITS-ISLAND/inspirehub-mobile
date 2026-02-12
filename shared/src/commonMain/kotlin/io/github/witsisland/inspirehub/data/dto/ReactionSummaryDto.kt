package io.github.witsisland.inspirehub.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 個別リアクション種別の集計DTO
 *
 * [ReactionsDto] の各フィールド、および `POST /nodes/{id}/like`,
 * `POST /nodes/{id}/interested`, `POST /nodes/{id}/want-to-try` のレスポンスとして使用。
 *
 * @property count このリアクション種別の総数
 * @property isReacted ログインユーザーがこのリアクションを付けているかどうか
 * @see ReactionSummary ドメインモデルへの変換先
 */
@Serializable
data class ReactionSummaryDto(
    val count: Int = 0,
    @SerialName("is_reacted")
    val isReacted: Boolean = false
)
