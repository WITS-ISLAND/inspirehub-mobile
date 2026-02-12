package io.github.witsisland.inspirehub.domain.model

import kotlinx.serialization.Serializable

/**
 * ノードの種別を表す列挙型
 *
 * InspireHubでは3種類のノードが存在する:
 * - [ISSUE]: 解決したい課題や「作りたいもの」の投稿
 * - [IDEA]: 課題に対する解決策、または単独のアイデア
 * - [PROJECT]: 具体的なプロジェクトとして進行中のもの
 *
 * APIとの対応: APIではsnake_case小文字（"idea", "issue", "project"）で表現される。
 * NodeMapperで文字列からの変換を行い、未知の値は [IDEA] にフォールバックする。
 */
@Serializable
enum class NodeType {
    ISSUE,
    IDEA,
    PROJECT
}
