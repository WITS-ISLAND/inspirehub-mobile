package io.github.witsisland.inspirehub.domain.model

import kotlinx.serialization.Serializable

/**
 * ノードのリアクション集計のドメインモデル
 *
 * InspireHubでは3種類のリアクションが存在する:
 * - [like]: いいね -- 賛同や支持を表す
 * - [interested]: 気になる -- 興味を持ったことを表す
 * - [wantToTry]: 作ってみたい -- 参加意欲を示す軽いコミットメント
 *
 * @property like いいねリアクションの集計
 * @property interested 気になるリアクションの集計
 * @property wantToTry 作ってみたいリアクションの集計
 */
@Serializable
data class Reactions(
    val like: ReactionSummary = ReactionSummary(),
    val interested: ReactionSummary = ReactionSummary(),
    val wantToTry: ReactionSummary = ReactionSummary()
)
