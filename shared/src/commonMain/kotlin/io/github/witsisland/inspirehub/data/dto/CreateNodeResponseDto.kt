package io.github.witsisland.inspirehub.data.dto

import kotlinx.serialization.Serializable

/**
 * `POST /nodes` のレスポンスDTO (HTTP 201 Created)
 *
 * ノード作成成功時に返却される。完全なノードデータは含まれず、
 * 作成されたノードのIDとメッセージのみ。詳細が必要な場合は
 * `GET /nodes/{id}` で別途取得する。
 *
 * @property id 作成されたノードの一意識別子
 * @property message 作成成功メッセージ（例: "Node created successfully"）
 */
@Serializable
data class CreateNodeResponseDto(
    val id: String,
    val message: String
)
