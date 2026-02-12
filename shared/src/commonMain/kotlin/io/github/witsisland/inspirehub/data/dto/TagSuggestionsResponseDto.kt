package io.github.witsisland.inspirehub.data.dto

import kotlinx.serialization.Serializable

/**
 * タグサジェスト候補の個別エントリ
 *
 * [TagDto] のサブセットで、usageCountやcreatedAtを含まない軽量版。
 *
 * @property id タグの一意識別子
 * @property name タグ名
 */
@Serializable
data class TagSuggestionDto(
    val id: String,
    val name: String
)

/**
 * `GET /tags/suggest` のレスポンスDTO
 *
 * クエリ文字列に基づくタグ候補を返す。
 * クエリパラメータ: q（必須）, limit
 *
 * @property suggestions タグ候補一覧
 */
@Serializable
data class TagSuggestionsResponseDto(
    val suggestions: List<TagSuggestionDto>
)
