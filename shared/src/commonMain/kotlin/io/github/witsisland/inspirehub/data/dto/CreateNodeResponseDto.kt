package io.github.witsisland.inspirehub.data.dto

import kotlinx.serialization.Serializable

/**
 * POST /nodes のレスポンス (201 Created)
 *
 * APIはノードの完全なデータではなく、作成されたノードのIDとメッセージのみ返す。
 * 完全なノードデータが必要な場合は GET /nodes/{id} で別途取得する。
 */
@Serializable
data class CreateNodeResponseDto(
    val id: String,
    val message: String
)
