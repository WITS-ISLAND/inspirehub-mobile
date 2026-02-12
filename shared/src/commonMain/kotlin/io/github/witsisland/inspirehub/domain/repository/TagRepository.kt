package io.github.witsisland.inspirehub.domain.repository

import io.github.witsisland.inspirehub.domain.model.Node
import io.github.witsisland.inspirehub.domain.model.Tag
import kotlin.native.HiddenFromObjC

/**
 * タグリポジトリ
 */
@HiddenFromObjC
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

    /**
     * タグ名でノード一覧を取得
     * @param tagName タグ名
     * @param limit 取得件数
     * @param offset オフセット
     */
    suspend fun getNodesByTagName(tagName: String, limit: Int = 20, offset: Int = 0): Result<List<Node>>
}
