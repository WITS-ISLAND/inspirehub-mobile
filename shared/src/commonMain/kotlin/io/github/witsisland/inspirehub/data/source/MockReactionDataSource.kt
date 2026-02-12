package io.github.witsisland.inspirehub.data.source

import io.github.witsisland.inspirehub.data.dto.ReactionSummaryDto
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

    override suspend fun toggleLike(nodeId: String): ReactionSummaryDto {
        return toggle(likes, nodeId)
    }

    override suspend fun toggleInterested(nodeId: String): ReactionSummaryDto {
        return toggle(interested, nodeId)
    }

    override suspend fun toggleWantToTry(nodeId: String): ReactionSummaryDto {
        return toggle(wantToTry, nodeId)
    }

    private fun toggle(store: MutableMap<String, ReactionState>, nodeId: String): ReactionSummaryDto {
        val state = store.getOrPut(nodeId) { ReactionState() }
        state.isReacted = !state.isReacted
        state.count = if (state.isReacted) state.count + 1 else state.count - 1
        return ReactionSummaryDto(count = state.count, isReacted = state.isReacted)
    }
}
