package io.github.witsisland.inspirehub.presentation.viewmodel

import app.cash.turbine.test
import io.github.witsisland.inspirehub.domain.model.Node
import io.github.witsisland.inspirehub.domain.model.NodeType
import io.github.witsisland.inspirehub.domain.model.User
import io.github.witsisland.inspirehub.domain.repository.FakeNodeRepository
import io.github.witsisland.inspirehub.domain.store.UserStore
import io.github.witsisland.inspirehub.test.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * MyPageViewModelの単体テスト
 */
class MyPageViewModelTest : MainDispatcherRule() {

    private lateinit var viewModel: MyPageViewModel
    private lateinit var fakeNodeRepository: FakeNodeRepository
    private lateinit var userStore: UserStore

    private val testUser = User(
        id = "user1",
        handle = "testuser",
        roleTag = "Backend"
    )

    private val sampleNodes = listOf(
        Node(
            id = "node1",
            type = NodeType.ISSUE,
            title = "自分の課題",
            content = "自分の投稿",
            authorId = "user1",
            authorName = "テストユーザー",
            commentCount = 2,
            createdAt = "2026-01-20T09:00:00Z",
            updatedAt = "2026-01-20T10:00:00Z"
        ),
        Node(
            id = "node2",
            type = NodeType.IDEA,
            title = "他人のアイデア",
            content = "他人の投稿",
            authorId = "user2",
            authorName = "他人ユーザー",
            commentCount = 0,
            createdAt = "2026-01-21T09:00:00Z",
            updatedAt = "2026-01-21T09:00:00Z"
        ),
        Node(
            id = "node3",
            type = NodeType.IDEA,
            title = "自分のアイデア",
            content = "自分の投稿2",
            authorId = "user1",
            authorName = "テストユーザー",
            commentCount = 1,
            createdAt = "2026-01-22T09:00:00Z",
            updatedAt = "2026-01-22T09:00:00Z"
        )
    )

    @BeforeTest
    fun setup() {
        fakeNodeRepository = FakeNodeRepository()
        userStore = UserStore()
        viewModel = MyPageViewModel(userStore, fakeNodeRepository)
    }

    @AfterTest
    fun tearDown() {
        userStore.logout()
        fakeNodeRepository.reset()
    }

    // ========================================
    // loadMyNodes のテスト
    // ========================================

    @Test
    fun `loadMyNodes - 自分のノードのみフィルタされること`() = runTest {
        // Given
        userStore.login(testUser, "access-token", "refresh-token")
        fakeNodeRepository.getNodesResult = Result.success(sampleNodes)

        // When
        viewModel.loadMyNodes()

        // Then
        assertEquals(2, viewModel.myNodes.value.size)
        assertTrue(viewModel.myNodes.value.all { it.authorId == "user1" })
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.error.value)
    }

    @Test
    fun `loadMyNodes - 未認証の場合エラーが設定されること`() = runTest {
        // Given: ユーザー未ログイン

        // When
        viewModel.loadMyNodes()

        // Then
        assertEquals("User not authenticated", viewModel.error.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `loadMyNodes - API失敗時にエラーが設定されること`() = runTest {
        // Given
        userStore.login(testUser, "access-token", "refresh-token")
        val errorMessage = "Network error"
        fakeNodeRepository.getNodesResult = Result.failure(Exception(errorMessage))

        // When
        viewModel.loadMyNodes()

        // Then
        assertEquals(errorMessage, viewModel.error.value)
        assertFalse(viewModel.isLoading.value)
    }

    // ========================================
    // currentUser のテスト
    // ========================================

    @Test
    fun `currentUser - UserStoreの状態が反映されること`() = runTest {
        // When
        userStore.login(testUser, "access-token", "refresh-token")

        // Then
        viewModel.currentUser.test {
            assertEquals(testUser, awaitItem())
        }
    }

    @Test
    fun `currentUser - ログアウト時にnullになること`() = runTest {
        // Given
        userStore.login(testUser, "access-token", "refresh-token")

        // When
        userStore.logout()

        // Then
        viewModel.currentUser.test {
            assertNull(awaitItem())
        }
    }

    // ========================================
    // refresh のテスト
    // ========================================

    @Test
    fun `refresh - loadMyNodesが呼ばれること`() = runTest {
        // Given
        userStore.login(testUser, "access-token", "refresh-token")
        fakeNodeRepository.getNodesResult = Result.success(sampleNodes)

        // When
        viewModel.refresh()

        // Then
        assertEquals(1, fakeNodeRepository.getNodesCallCount)
    }

    // ========================================
    // 初期状態のテスト
    // ========================================

    @Test
    fun `初期状態 - ノードが空でユーザーがnullであること`() = runTest {
        // Then
        assertNull(viewModel.currentUser.value)
        assertTrue(viewModel.myNodes.value.isEmpty())
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.error.value)
    }
}
