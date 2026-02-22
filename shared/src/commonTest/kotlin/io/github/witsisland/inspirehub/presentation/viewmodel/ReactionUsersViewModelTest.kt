package io.github.witsisland.inspirehub.presentation.viewmodel

import io.github.witsisland.inspirehub.domain.model.ReactedUser
import io.github.witsisland.inspirehub.domain.model.ReactedUsersPage
import io.github.witsisland.inspirehub.domain.model.ReactionType
import io.github.witsisland.inspirehub.domain.repository.FakeReactionRepository
import io.github.witsisland.inspirehub.test.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ReactionUsersViewModelTest : MainDispatcherRule() {

    private lateinit var viewModel: ReactionUsersViewModel
    private lateinit var fakeReactionRepository: FakeReactionRepository

    private val sampleUsers = listOf(
        ReactedUser(
            userId = "user1",
            userName = "テストユーザー1",
            userPicture = null,
            reactedAt = "2026-01-20T09:00:00Z"
        ),
        ReactedUser(
            userId = "user2",
            userName = "テストユーザー2",
            userPicture = "https://example.com/user2.png",
            reactedAt = "2026-01-20T10:00:00Z"
        )
    )

    private val samplePage = ReactedUsersPage(
        users = sampleUsers,
        nextCursor = null,
        hasMore = false,
        total = 2
    )

    @BeforeTest
    fun setup() {
        fakeReactionRepository = FakeReactionRepository()
        viewModel = ReactionUsersViewModel(fakeReactionRepository)
    }

    @Test
    fun `loadUsers - 成功時にユーザー一覧が設定されること`() = runTest {
        fakeReactionRepository.getReactedUsersResult = Result.success(samplePage)

        viewModel.loadUsers(nodeId = "node1", type = ReactionType.LIKE)

        assertEquals(sampleUsers, viewModel.users.value)
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.error.value)
        assertEquals(2, viewModel.total.value)
        assertFalse(viewModel.hasMore.value)
    }

    @Test
    fun `loadUsers - 正しい引数でRepositoryが呼ばれること`() = runTest {
        fakeReactionRepository.getReactedUsersResult = Result.success(samplePage)

        viewModel.loadUsers(nodeId = "node-abc", type = ReactionType.INTERESTED)

        assertEquals("node-abc", fakeReactionRepository.lastGetReactedUsersNodeId)
        assertEquals(ReactionType.INTERESTED, fakeReactionRepository.lastGetReactedUsersType)
        assertNull(fakeReactionRepository.lastGetReactedUsersCursor)
    }

    @Test
    fun `loadUsers - 失敗時にエラーが設定されること`() = runTest {
        fakeReactionRepository.shouldReturnError = true
        fakeReactionRepository.errorMessage = "ネットワークエラー"

        viewModel.loadUsers(nodeId = "node1", type = ReactionType.LIKE)

        assertTrue(viewModel.users.value.isEmpty())
        assertEquals("ネットワークエラー", viewModel.error.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `loadUsers - 再呼び出し時にリストがリセットされること`() = runTest {
        fakeReactionRepository.getReactedUsersResult = Result.success(samplePage)
        viewModel.loadUsers(nodeId = "node1", type = ReactionType.LIKE)
        assertEquals(2, viewModel.users.value.size)

        fakeReactionRepository.getReactedUsersResult = Result.success(
            samplePage.copy(users = listOf(sampleUsers[0]), total = 1)
        )
        viewModel.loadUsers(nodeId = "node2", type = ReactionType.WANT_TO_TRY)

        assertEquals(1, viewModel.users.value.size)
        assertEquals("user1", viewModel.users.value[0].userId)
    }

    @Test
    fun `loadMore - hasMoreがtrueの場合に追加ページが読み込まれること`() = runTest {
        val firstPage = ReactedUsersPage(
            users = listOf(sampleUsers[0]),
            nextCursor = "cursor-abc",
            hasMore = true,
            total = 2
        )
        val secondPage = ReactedUsersPage(
            users = listOf(sampleUsers[1]),
            nextCursor = null,
            hasMore = false,
            total = 2
        )

        fakeReactionRepository.getReactedUsersResult = Result.success(firstPage)
        viewModel.loadUsers(nodeId = "node1", type = ReactionType.LIKE)
        assertEquals(1, viewModel.users.value.size)

        fakeReactionRepository.getReactedUsersResult = Result.success(secondPage)
        viewModel.loadMore()

        assertEquals(2, viewModel.users.value.size)
        assertFalse(viewModel.hasMore.value)
        assertEquals(2, fakeReactionRepository.getReactedUsersCallCount)
        assertEquals("cursor-abc", fakeReactionRepository.lastGetReactedUsersCursor)
    }

    @Test
    fun `loadMore - hasMoreがfalseの場合はRepositoryを呼ばないこと`() = runTest {
        fakeReactionRepository.getReactedUsersResult = Result.success(samplePage)
        viewModel.loadUsers(nodeId = "node1", type = ReactionType.LIKE)

        viewModel.loadMore()

        assertEquals(1, fakeReactionRepository.getReactedUsersCallCount)
    }

    @Test
    fun `isLoading - loadUsers完了後にfalseになること`() = runTest {
        fakeReactionRepository.getReactedUsersResult = Result.success(samplePage)

        viewModel.loadUsers(nodeId = "node1", type = ReactionType.LIKE)

        assertFalse(viewModel.isLoading.value)
    }
}
