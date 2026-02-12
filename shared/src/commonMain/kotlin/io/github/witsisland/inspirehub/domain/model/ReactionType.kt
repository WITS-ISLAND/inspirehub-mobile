package io.github.witsisland.inspirehub.domain.model

import kotlinx.serialization.Serializable

/**
 * リアクションの種別を表す列挙型
 *
 * APIエンドポイントとの対応:
 * - [LIKE] -- `POST /nodes/{id}/like`
 * - [INTERESTED] -- `POST /nodes/{id}/interested`
 * - [WANT_TO_TRY] -- `POST /nodes/{id}/want-to-try`
 */
@Serializable
enum class ReactionType {
    LIKE,
    INTERESTED,
    WANT_TO_TRY
}
