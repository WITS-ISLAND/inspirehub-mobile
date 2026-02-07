package io.github.witsisland.inspirehub.presentation.viewmodel

import app.cash.turbine.test
import io.github.witsisland.inspirehub.domain.model.Node
import io.github.witsisland.inspirehub.domain.model.NodeType
import io.github.witsisland.inspirehub.domain.model.User
import io.github.witsisland.inspirehub.domain.repository.FakeAuthRepository
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
    private lateinit var fakeAuthRepository: FakeAuthRepository
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

    private val reactedSampleNodes = listOf(
        Node(
            id = "node4",
            type = NodeType.IDEA,
            title = "リアクション済みノード",
            content = "リアクションした投稿",
            authorId = "user2",
            authorName = "他人ユーザー",
            commentCount = 0,
            createdAt = "2026-01-23T09:00:00Z",
            updatedAt = "2026-01-23T09:00:00Z"
        )
    )

    @BeforeTest
    fun setup() {
        fakeNodeRepository = FakeNodeRepository()
        fakeAuthRepository = FakeAuthRepository()
        userStore = UserStore()
        viewModel = MyPageViewModel(userStore, fakeNodeRepository, fakeAuthRepository)
    }

    @AfterTest
    fun tearDown() {
        userStore.logout()
        fakeNodeRepository.reset()
        fakeAuthRepository.reset()
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
    // loadReactedNodes のテスト (BUG-003)
    // ========================================

    @Test
    fun `loadReactedNodes - リアクション済みノードが読み込まれること`() = runTest {
        // Given
        fakeNodeRepository.getReactedNodesResult = Result.success(reactedSampleNodes)

        // When
        viewModel.loadReactedNodes()

        // Then
        assertEquals(1, viewModel.reactedNodes.value.size)
        assertEquals("node4", viewModel.reactedNodes.value.first().id)
        assertEquals(1, fakeNodeRepository.getReactedNodesCallCount)
    }

    @Test
    fun `loadReactedNodes - API失敗時にreactedNodesは空のまま`() = runTest {
        // Given
        fakeNodeRepository.getReactedNodesResult = Result.failure(Exception("Network error"))

        // When
        viewModel.loadReactedNodes()

        // Then
        assertTrue(viewModel.reactedNodes.value.isEmpty())
    }

    // ========================================
    // logout のテスト (BUG-008)
    // ========================================

    @Test
    fun `logout - ログアウトが成功すること`() = runTest {
        // Given
        userStore.login(testUser, "access-token", "refresh-token")
        fakeAuthRepository.logoutResult = Result.success(Unit)

        // When
        viewModel.logout()

        // Then
        assertEquals(1, fakeAuthRepository.logoutCallCount)
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.error.value)
    }

    @Test
    fun `logout - ログアウト失敗時にエラーが設定されること`() = runTest {
        // Given
        fakeAuthRepository.logoutResult = Result.failure(Exception("Logout failed"))

        // When
        viewModel.logout()

        // Then
        assertEquals("Logout failed", viewModel.error.value)
        assertFalse(viewModel.isLoading.value)
    }

    // ========================================
    // プロフィール名編集 のテスト (#26)
    // ========================================

    @Test
    fun `startEditingName - 現在のユーザー名が設定されること`() = runTest {
        // Given
        userStore.login(testUser, "access-token", "refresh-token")
        // UserStoreのcollectが反映されるまで待つ
        viewModel.currentUser.test { awaitItem() }

        // When
        viewModel.startEditingName()

        // Then
        assertTrue(viewModel.isEditingName.value)
        assertEquals("testuser", viewModel.editingName.value)
    }

    @Test
    fun `cancelEditingName - 編集状態がリセットされること`() = runTest {
        // Given
        viewModel.startEditingName()

        // When
        viewModel.cancelEditingName()

        // Then
        assertFalse(viewModel.isEditingName.value)
        assertEquals("", viewModel.editingName.value)
    }

    @Test
    fun `updateEditingName - 編集中の名前が更新されること`() = runTest {
        // When
        viewModel.updateEditingName("新しい名前")

        // Then
        assertEquals("新しい名前", viewModel.editingName.value)
    }

    @Test
    fun `updateUserName - 名前更新が成功すること`() = runTest {
        // Given
        val updatedUser = testUser.copy(handle = "新しい名前")
        fakeAuthRepository.updateUserNameResult = Result.success(updatedUser)
        viewModel.updateEditingName("新しい名前")

        // When
        viewModel.updateUserName()

        // Then
        assertEquals(1, fakeAuthRepository.updateUserNameCallCount)
        assertEquals("新しい名前", fakeAuthRepository.lastUpdateUserName)
        assertFalse(viewModel.isEditingName.value)
        assertEquals("", viewModel.editingName.value)
        assertFalse(viewModel.isUpdatingName.value)
        assertNull(viewModel.error.value)
    }

    @Test
    fun `updateUserName - 空文字の場合エラーが設定されること`() = runTest {
        // Given
        viewModel.updateEditingName("  ")

        // When
        viewModel.updateUserName()

        // Then
        assertEquals("名前を入力してください", viewModel.error.value)
        assertEquals(0, fakeAuthRepository.updateUserNameCallCount)
    }

    @Test
    fun `updateUserName - API失敗時にエラーが設定されること`() = runTest {
        // Given
        fakeAuthRepository.updateUserNameResult = Result.failure(Exception("Update failed"))
        viewModel.startEditingName()
        viewModel.updateEditingName("新しい名前")

        // When
        viewModel.updateUserName()

        // Then
        assertEquals("Update failed", viewModel.error.value)
        assertTrue(viewModel.isEditingName.value) // 編集モードのまま
        assertFalse(viewModel.isUpdatingName.value)
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
    fun `refresh - loadMyNodesとloadReactedNodesが呼ばれること`() = runTest {
        // Given
        userStore.login(testUser, "access-token", "refresh-token")
        fakeNodeRepository.getNodesResult = Result.success(sampleNodes)
        fakeNodeRepository.getReactedNodesResult = Result.success(reactedSampleNodes)

        // When
        viewModel.refresh()

        // Then
        assertEquals(1, fakeNodeRepository.getNodesCallCount)
        assertEquals(1, fakeNodeRepository.getReactedNodesCallCount)
    }

    // ========================================
    // 初期状態のテスト
    // ========================================

    @Test
    fun `初期状態 - ノードが空でユーザーがnullであること`() = runTest {
        // Then
        assertNull(viewModel.currentUser.value)
        assertTrue(viewModel.myNodes.value.isEmpty())
        assertTrue(viewModel.reactedNodes.value.isEmpty())
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.error.value)
        assertFalse(viewModel.isEditingName.value)
        assertEquals("", viewModel.editingName.value)
        assertFalse(viewModel.isUpdatingName.value)
    }
}
