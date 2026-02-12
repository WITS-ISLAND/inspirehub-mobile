package io.github.witsisland.inspirehub.domain.model

import kotlinx.serialization.Serializable

/**
 * タグのドメインモデル
 *
 * ノードの分類・検索に使用されるタグ。ユーザーが自由に作成でき、
 * 人気タグやサジェストで再利用が促進される。
 *
 * @property id タグの一意識別子
 * @property name タグ名
 * @property usageCount このタグが付与されているノード数（DTOでnullの場合0にフォールバック）
 * @property createdAt タグの作成日時（エンドポイントによっては取得できない場合がありnullable）
 */
@Serializable
data class Tag(
    val id: String,
    val name: String,
    val usageCount: Int = 0,
    val createdAt: String? = null
)
