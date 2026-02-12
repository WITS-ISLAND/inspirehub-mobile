package io.github.witsisland.inspirehub.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * タグのDTO
 *
 * `GET /tags`, `GET /tags/popular` のレスポンス配列要素、および
 * [NodeDto.tags] の要素として使用される。
 *
 * @property id タグの一意識別子
 * @property name タグ名
 * @property usageCount このタグが使用されているノード数（エンドポイントによってはnull）
 * @property createdAt タグの作成日時（ISO 8601形式。[NodeDto.tags] 内では空文字の場合あり）
 * @see Tag ドメインモデルへの変換先
 */
@Serializable
data class TagDto(
    val id: String,
    val name: String,
    @SerialName("usage_count")
    val usageCount: Int? = null,
    @SerialName("created_at")
    val createdAt: String = ""
)
