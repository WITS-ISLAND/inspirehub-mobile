package io.github.witsisland.inspirehub.data.repository

import io.github.witsisland.inspirehub.data.source.ReactionDataSource
import io.github.witsisland.inspirehub.domain.model.ReactionSummary
import io.github.witsisland.inspirehub.domain.model.ReactionType
import io.github.witsisland.inspirehub.domain.repository.ReactionRepository
import kotlin.native.HiddenFromObjC

@HiddenFromObjC
class ReactionRepositoryImpl(
    private val dataSource: ReactionDataSource
) : ReactionRepository {

    override suspend fun toggleReaction(nodeId: String, type: ReactionType): Result<ReactionSummary> {
        return try {
            val dto = when (type) {
                ReactionType.LIKE -> dataSource.toggleLike(nodeId)
                ReactionType.INTERESTED -> dataSource.toggleInterested(nodeId)
                ReactionType.WANT_TO_TRY -> dataSource.toggleWantToTry(nodeId)
            }
            Result.success(ReactionSummary(count = dto.count, isReacted = dto.isReacted))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
