package io.github.witsisland.inspirehub.presentation.viewmodel

import app.cash.turbine.test
import io.github.witsisland.inspirehub.domain.model.Node
import io.github.witsisland.inspirehub.domain.model.NodeType
import io.github.witsisland.inspirehub.domain.model.Reactions
import io.github.witsisland.inspirehub.domain.model.ReactionSummary
import io.github.witsisland.inspirehub.domain.repository.FakeNodeRepository
import io.github.witsisland.inspirehub.domain.store.HomeTab
import io.github.witsisland.inspirehub.domain.store.NodeStore
import io.github.witsisland.inspirehub.domain.store.SortOrder
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

class HomeViewModelTest : MainDispatcherRule() {

    private lateinit var viewModel: HomeViewModel
    private lateinit var fakeNodeRepository: FakeNodeRepository
    private lateinit var nodeStore: NodeStore
    private lateinit var userStore: UserStore

    private val sampleNodes = listOf(
        Node(
            id = "node1",
            type = NodeType.ISSUE,
            title = "課題1",
            content = "課題の内容1",
            authorId = "user1",
            authorName = "テストユーザー1",
            reactions = Reactions(like = ReactionSummary(count = 5)),
            commentCount = 2,
            createdAt = "2026-01-20T09:00:00Z",
            updatedAt = "2026-01-20T10:00:00Z"
        ),
        Node(
            id = "node2",
            type = NodeType.IDEA,
            title = "アイデア1",
            content = "アイデアの内容1",
            authorId = "user2",
            authorName = "テストユーザー2",
            reactions = Reactions(like = ReactionSummary(count = 10, isReacted = true)),
            commentCount = 0,
            createdAt = "2026-01-21T09:00:00Z",
            updatedAt = "2026-01-21T09:00:00Z"
        ),
        Node(
            id = "node3",
            type = NodeType.ISSUE,
            title = "課題2",
            content = "課題の内容2",
            authorId = "user1",
            authorName = "テストユーザー1",
            reactions = Reactions(like = ReactionSummary(count = 3)),
            commentCount = 1,
            createdAt = "2026-01-22T09:00:00Z",
            updatedAt = "2026-01-22T09:00:00Z"
        )
    )

    @BeforeTest
    fun setup() {
        fakeNodeRepository = FakeNodeRepository()
        nodeStore = NodeStore()
        userStore = UserStore()
        viewModel = HomeViewModel(nodeStore, fakeNodeRepository, userStore)
    }

    @AfterTest
    fun tearDown() {
        nodeStore.clear()
        fakeNodeRepository.reset()
    }

    @Test
    fun `loadNodes - ノード一覧の取得が成功すること`() = runTest {
        fakeNodeRepository.getNodesResult = Result.success(sampleNodes)

        viewModel.loadNodes()

        assertEquals(1, fakeNodeRepository.getNodesCallCount)
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.error.value)
        assertEquals(sampleNodes.size, viewModel.nodes.value.size)
    }

    @Test
    fun `loadNodes - 失敗時にエラーが設定されること`() = runTest {
        val errorMessage = "Network error"
        fakeNodeRepository.getNodesResult = Result.failure(Exception(errorMessage))

        viewModel.loadNodes()

        assertEquals(errorMessage, viewModel.error.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `loadNodes - ノードがcreatedAt降順でソートされること`() = runTest {
        fakeNodeRepository.getNodesResult = Result.success(sampleNodes)

        viewModel.loadNodes()

        val result = viewModel.nodes.value
        assertEquals("node3", result[0].id)
        assertEquals("node2", result[1].id)
        assertEquals("node1", result[2].id)
    }

    @Test
    fun `setTab - ISSUESタブで課題のみフィルタされること`() = runTest {
        fakeNodeRepository.getNodesResult = Result.success(sampleNodes)
        viewModel.loadNodes()

        viewModel.setTab(HomeTab.ISSUES)

        viewModel.currentTab.test {
            assertEquals(HomeTab.ISSUES, awaitItem())
        }
        val filtered = viewModel.nodes.value
        assertTrue(filtered.all { it.type == NodeType.ISSUE })
        assertEquals(2, filtered.size)
    }

    @Test
    fun `setTab - IDEASタブでアイデアのみフィルタされること`() = runTest {
        fakeNodeRepository.getNodesResult = Result.success(sampleNodes)
        viewModel.loadNodes()

        viewModel.setTab(HomeTab.IDEAS)

        val filtered = viewModel.nodes.value
        assertTrue(filtered.all { it.type == NodeType.IDEA })
        assertEquals(1, filtered.size)
    }

    @Test
    fun `setTab - RECENTタブで全ノードが表示されること`() = runTest {
        fakeNodeRepository.getNodesResult = Result.success(sampleNodes)
        viewModel.loadNodes()
        viewModel.setTab(HomeTab.ISSUES)

        viewModel.setTab(HomeTab.RECENT)

        assertEquals(sampleNodes.size, viewModel.nodes.value.size)
    }

    @Test
    fun `setSortOrder - RECENT順で最新が先頭になること`() = runTest {
        fakeNodeRepository.getNodesResult = Result.success(sampleNodes)
        viewModel.loadNodes()

        viewModel.setSortOrder(SortOrder.RECENT)

        viewModel.sortOrder.test {
            assertEquals(SortOrder.RECENT, awaitItem())
        }
        val result = viewModel.nodes.value
        assertEquals("node3", result.first().id)
    }

    @Test
    fun `setSortOrder - ソート順が変更されること`() = runTest {
        fakeNodeRepository.getNodesResult = Result.success(sampleNodes)
        viewModel.loadNodes()

        viewModel.setSortOrder(SortOrder.POPULAR)

        viewModel.sortOrder.test {
            assertEquals(SortOrder.POPULAR, awaitItem())
        }
    }

    @Test
    fun `toggleLike - いいね切り替えが成功すること`() = runTest {
        fakeNodeRepository.getNodesResult = Result.success(sampleNodes)
        val toggledNode = sampleNodes[0].copy(
            reactions = Reactions(like = ReactionSummary(count = 6, isReacted = true))
        )
        fakeNodeRepository.toggleLikeResult = Result.success(toggledNode)

        viewModel.toggleLike("node1")

        assertEquals(1, fakeNodeRepository.toggleLikeCallCount)
        assertEquals("node1", fakeNodeRepository.lastToggleLikeNodeId)
        assertNull(viewModel.error.value)
    }

    @Test
    fun `toggleLike - 失敗時にエラーが設定されること`() = runTest {
        val errorMessage = "Like failed"
        fakeNodeRepository.toggleLikeResult = Result.failure(Exception(errorMessage))

        viewModel.toggleLike("node1")

        assertEquals(errorMessage, viewModel.error.value)
    }

    @Test
    fun `refresh - loadNodesが呼ばれること`() = runTest {
        fakeNodeRepository.getNodesResult = Result.success(sampleNodes)

        viewModel.refresh()

        assertEquals(1, fakeNodeRepository.getNodesCallCount)
        assertNull(viewModel.error.value)
    }

    @Test
    fun `初期状態 - ノードが空で、タブがRECENTであること`() = runTest {
        assertTrue(viewModel.nodes.value.isEmpty())
        assertEquals(HomeTab.RECENT, viewModel.currentTab.value)
        assertEquals(SortOrder.RECENT, viewModel.sortOrder.value)
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.error.value)
    }
}
