package io.github.witsisland.inspirehub.data.source

import io.github.witsisland.inspirehub.data.dto.TagDto

/**
 * タグデータソースインターフェース
 */
interface TagDataSource {
    /**
     * 人気タグ一覧を取得
     * @param limit 取得件数
     */
    suspend fun getPopularTags(limit: Int = 10): List<TagDto>

    /**
     * タグをサジェスト
     * @param query 検索クエリ
     * @param limit 取得件数
     */
    suspend fun suggestTags(query: String, limit: Int = 10): List<TagDto>
}
