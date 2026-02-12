package io.github.witsisland.inspirehub.data.dto

import kotlinx.serialization.Serializable

/**
 * コメント内のメンションユーザーDTO
 *
 * [CommentDto.mentions] の配列要素として使用される。
 * ドメインモデルへの変換時は [name] のみが抽出される（[Comment.mentions] は `List<String>`）。
 *
 * @property id メンションされたユーザーのID
 * @property name メンションされたユーザーの表示名
 * @property picture メンションされたユーザーのプロフィール画像URL
 * @see Comment.mentions ドメインモデルでの表現（名前のみのリスト）
 */
@Serializable
data class MentionDto(
    val id: String,
    val name: String,
    val picture: String? = null
)
