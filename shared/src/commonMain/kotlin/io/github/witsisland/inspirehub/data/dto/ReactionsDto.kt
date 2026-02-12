package io.github.witsisland.inspirehub.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * ノードのリアクション集計DTO
 *
 * [NodeDto.reactions] として使用される。3種類のリアクション（いいね・共感・作ってみたい）
 * それぞれのカウントとログインユーザーのリアクション状態を保持する。
 *
 * @property like いいねリアクション（API key: "like"）
 * @property interested 共感リアクション（API key: "interested"）
 * @property wantToTry 作ってみたいリアクション（API key: "want_to_try"）
 * @see Reactions ドメインモデルへの変換先
 */
@Serializable
data class ReactionsDto(
    val like: ReactionSummaryDto = ReactionSummaryDto(),
    val interested: ReactionSummaryDto = ReactionSummaryDto(),
    @SerialName("want_to_try")
    val wantToTry: ReactionSummaryDto = ReactionSummaryDto()
)
