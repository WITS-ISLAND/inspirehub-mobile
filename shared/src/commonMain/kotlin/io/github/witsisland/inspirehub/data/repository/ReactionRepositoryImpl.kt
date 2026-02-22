package io.github.witsisland.inspirehub.data.repository

import io.github.witsisland.inspirehub.data.source.ReactionDataSource
import io.github.witsisland.inspirehub.domain.model.ReactedUser
import io.github.witsisland.inspirehub.domain.model.ReactedUsersPage
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

    override suspend fun getReactedUsers(
        nodeId: String,
        type: ReactionType,
        limit: Int,
        cursor: String?
    ): Result<ReactedUsersPage> {
        return try {
            val dto = dataSource.getReactionUsers(nodeId, type, limit, cursor)
            val users = dto.data.map { userDto ->
                ReactedUser(
                    userId = userDto.userId,
                    userName = userDto.userName,
                    userPicture = userDto.userPicture,
                    reactedAt = userDto.reactedAt
                )
            }
            Result.success(
                ReactedUsersPage(
                    users = users,
                    nextCursor = dto.nextCursor,
                    hasMore = dto.hasMore,
                    total = dto.total
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
