package io.github.witsisland.inspirehub.data.source

import io.github.witsisland.inspirehub.data.dto.NodeDto
import io.github.witsisland.inspirehub.data.dto.TagDto
import kotlin.native.HiddenFromObjC

/**
 * タグデータソースインターフェース
 */
@HiddenFromObjC
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

    /**
     * タグ名でノード一覧を取得
     * @param tagName タグ名
     * @param limit 取得件数
     * @param offset オフセット
     */
    suspend fun getNodesByTagName(tagName: String, limit: Int = 20, offset: Int = 0): List<NodeDto>
}
