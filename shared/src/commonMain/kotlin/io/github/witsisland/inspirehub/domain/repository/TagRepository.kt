package io.github.witsisland.inspirehub.domain.repository

import io.github.witsisland.inspirehub.domain.model.Tag

/**
 * タグリポジトリ
 */
interface TagRepository {
    /**
     * 人気タグ一覧を取得
     * @param limit 取得件数
     */
    suspend fun getPopularTags(limit: Int = 10): Result<List<Tag>>

    /**
     * タグをサジェスト
     * @param query 検索クエリ
     * @param limit 取得件数
     */
    suspend fun suggestTags(query: String, limit: Int = 10): Result<List<Tag>>
}
