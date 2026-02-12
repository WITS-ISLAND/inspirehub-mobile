package io.github.witsisland.inspirehub.domain.model

import kotlinx.serialization.Serializable

/**
 * コメントのドメインモデル
 *
 * ノードに対するコメントおよびコメントへの返信を表す。
 * [replies] による再帰構造でスレッド形式の会話を表現する。
 *
 * @property id コメントの一意識別子
 * @property nodeId コメントが属するノードのID
 * @property parentId 返信先コメントのID（トップレベルコメントの場合null）
 * @property authorId 投稿者のユーザーID
 * @property authorName 投稿者の表示名
 * @property authorPicture 投稿者のプロフィール画像URL
 * @property content コメント本文
 * @property mentions メンションされたユーザーの表示名一覧。
 *                    DTOの [MentionDto] からnameのみ抽出したもの（id, pictureは失われる）
 * @property replies このコメントへの返信一覧（再帰構造）
 * @property createdAt 作成日時（ISO 8601形式の文字列）
 */
@Serializable
data class Comment(
    val id: String,
    val nodeId: String,
    val parentId: String? = null,
    val authorId: String,
    val authorName: String,
    val authorPicture: String? = null,
    val content: String,
    val mentions: List<String> = emptyList(),
    val replies: List<Comment> = emptyList(),
    val createdAt: String
)
