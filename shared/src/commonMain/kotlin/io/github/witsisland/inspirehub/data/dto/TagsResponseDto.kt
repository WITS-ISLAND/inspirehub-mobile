package io.github.witsisland.inspirehub.data.dto

import kotlinx.serialization.Serializable

/**
 * `GET /tags/popular` および `GET /tags` のレスポンスDTO
 *
 * タグ一覧をページネーション付きで返す。
 * `GET /tags/popular` では使用頻度の高いタグが返却される。
 *
 * @property tags タグ一覧
 * @property total タグの総数（ページネーション用）
 * @see TagDto 個別タグのデータ構造
 */
@Serializable
data class TagsResponseDto(
    val tags: List<TagDto>,
    val total: Int
)
