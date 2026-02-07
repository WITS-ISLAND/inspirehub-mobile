package io.github.witsisland.inspirehub.presentation.viewmodel

import io.github.witsisland.inspirehub.domain.model.Node
import io.github.witsisland.inspirehub.domain.model.NodeType
import io.github.witsisland.inspirehub.domain.model.ParentNode
import io.github.witsisland.inspirehub.domain.repository.FakeNodeRepository
import io.github.witsisland.inspirehub.domain.store.NodeStore
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
 * MapViewModelの単体テスト
 */
class MapViewModelTest : MainDispatcherRule() {

    private lateinit var viewModel: MapViewModel
    private lateinit var fakeNodeRepository: FakeNodeRepository
    private lateinit var nodeStore: NodeStore

    private val sampleNodes = listOf(
        Node(
            id = "node1",
            type = NodeType.ISSUE,
            title = "課題1",
            content = "課題の内容1",
            authorId = "user1",
            authorName = "テストユーザー1",
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
            parentNode = ParentNode(id = "node1", type = NodeType.ISSUE, title = "課題1"),
            commentCount = 0,
            createdAt = "2026-01-21T09:00:00Z",
            updatedAt = "2026-01-21T09:00:00Z"
        )
    )

    @BeforeTest
    fun setup() {
        fakeNodeRepository = FakeNodeRepository()
        nodeStore = NodeStore()
        viewModel = MapViewModel(nodeStore, fakeNodeRepository)
    }

    @AfterTest
    fun tearDown() {
        nodeStore.clear()
        fakeNodeRepository.reset()
    }

    // ========================================
    // loadNodes のテスト
    // ========================================

    @Test
    fun `loadNodes - ノード一覧の取得が成功すること`() = runTest {
        // Given
        fakeNodeRepository.getNodesResult = Result.success(sampleNodes)

        // When
        viewModel.loadNodes()

        // Then
        assertEquals(1, fakeNodeRepository.getNodesCallCount)
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.error.value)
        assertEquals(sampleNodes.size, viewModel.nodes.value.size)
    }

    @Test
    fun `loadNodes - 失敗時にエラーが設定されること`() = runTest {
        // Given
        val errorMessage = "Network error"
        fakeNodeRepository.getNodesResult = Result.failure(Exception(errorMessage))

        // When
        viewModel.loadNodes()

        // Then
        assertEquals(errorMessage, viewModel.error.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `loadNodes - NodeStoreに反映されること`() = runTest {
        // Given
        fakeNodeRepository.getNodesResult = Result.success(sampleNodes)

        // When
        viewModel.loadNodes()

        // Then
        assertEquals(sampleNodes.size, nodeStore.nodes.value.size)
    }

    // ========================================
    // 初期状態のテスト
    // ========================================

    @Test
    fun `初期状態 - ノードが空であること`() = runTest {
        // Then
        assertTrue(viewModel.nodes.value.isEmpty())
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.error.value)
    }
}
