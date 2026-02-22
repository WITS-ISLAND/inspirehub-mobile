package io.github.witsisland.inspirehub.data.source

import io.github.witsisland.inspirehub.data.dto.ReactedUserDto
import io.github.witsisland.inspirehub.data.dto.ReactedUsersResponseDto
import io.github.witsisland.inspirehub.data.dto.ReactionSummaryDto
import io.github.witsisland.inspirehub.domain.model.ReactionType
import kotlin.native.HiddenFromObjC

@HiddenFromObjC
class MockReactionDataSource : ReactionDataSource {

    private data class ReactionState(
        var count: Int = 0,
        var isReacted: Boolean = false
    )

    private val likes = mutableMapOf<String, ReactionState>()
    private val interested = mutableMapOf<String, ReactionState>()
    private val wantToTry = mutableMapOf<String, ReactionState>()

    /** ダミーユーザーリスト（全リアクション種別共通） */
    private val dummyReactionUsers = listOf(
        ReactedUserDto(
            userId = "user-001",
            userName = "Alice",
            userPicture = "https://example.com/pictures/alice.png",
            reactedAt = "2026-01-15T10:00:00Z"
        ),
        ReactedUserDto(
            userId = "user-002",
            userName = "Bob",
            userPicture = null,
            reactedAt = "2026-01-16T14:30:00Z"
        ),
        ReactedUserDto(
            userId = "user-003",
            userName = null,
            userPicture = "https://example.com/pictures/carol.png",
            reactedAt = "2026-01-17T09:15:00Z"
        )
    )

    override suspend fun toggleLike(nodeId: String): ReactionSummaryDto {
        return toggle(likes, nodeId)
    }

    override suspend fun toggleInterested(nodeId: String): ReactionSummaryDto {
        return toggle(interested, nodeId)
    }

    override suspend fun toggleWantToTry(nodeId: String): ReactionSummaryDto {
        return toggle(wantToTry, nodeId)
    }

    override suspend fun getReactionUsers(
        nodeId: String,
        type: ReactionType,
        limit: Int,
        cursor: String?
    ): ReactedUsersResponseDto {
        return ReactedUsersResponseDto(
            data = dummyReactionUsers.take(limit),
            nextCursor = null,
            hasMore = false,
            total = dummyReactionUsers.size
        )
    }

    private fun toggle(store: MutableMap<String, ReactionState>, nodeId: String): ReactionSummaryDto {
        val state = store.getOrPut(nodeId) { ReactionState() }
        state.isReacted = !state.isReacted
        state.count = if (state.isReacted) state.count + 1 else state.count - 1
        return ReactionSummaryDto(count = state.count, isReacted = state.isReacted)
    }
}
