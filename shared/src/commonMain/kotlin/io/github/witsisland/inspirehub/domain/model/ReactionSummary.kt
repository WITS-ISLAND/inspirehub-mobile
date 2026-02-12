package io.github.witsisland.inspirehub.domain.model

import kotlinx.serialization.Serializable

/**
 * 個別リアクション種別の集計のドメインモデル
 *
 * リアクションの総数と、ログインユーザー自身がリアクション済みかどうかを保持する。
 *
 * @property count このリアクション種別の総数
 * @property isReacted ログインユーザーがこのリアクションを付けているかどうか
 */
@Serializable
data class ReactionSummary(
    val count: Int = 0,
    val isReacted: Boolean = false
)
