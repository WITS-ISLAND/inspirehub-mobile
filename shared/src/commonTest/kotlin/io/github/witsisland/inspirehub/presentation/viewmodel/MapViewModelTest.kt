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
    // getNodeTree のテスト
    // ========================================

    @Test
    fun `getNodeTree - ルートノードと子ノードのツリーが正しく構築されること`() = runTest {
        // Given
        fakeNodeRepository.getNodesResult = Result.success(sampleNodes)
        viewModel.loadNodes()

        // When
        val tree = viewModel.getNodeTree()

        // Then: node1はルート(depth=0), node2はnode1の子(depth=1)
        assertEquals(2, tree.size)
        val root = tree[0]
        assertEquals("node1", root.node.id)
        assertEquals(0, root.depth)
        assertEquals(1, root.childCount)

        val child = tree[1]
        assertEquals("node2", child.node.id)
        assertEquals(1, child.depth)
        assertEquals(0, child.childCount)
    }

    @Test
    fun `getNodeTree - ノードが空の場合は空リストが返ること`() = runTest {
        // Given: ノード未読み込み

        // When
        val tree = viewModel.getNodeTree()

        // Then
        assertTrue(tree.isEmpty())
    }

    @Test
    fun `getNodeTree - 全てルートノードの場合はdepth0で並ぶこと`() = runTest {
        // Given
        val rootOnlyNodes = listOf(
            Node(
                id = "r1",
                type = NodeType.ISSUE,
                title = "ルート1",
                content = "内容1",
                authorId = "user1",
                authorName = "ユーザー1",
                commentCount = 0,
                createdAt = "2026-01-20T09:00:00Z"
            ),
            Node(
                id = "r2",
                type = NodeType.ISSUE,
                title = "ルート2",
                content = "内容2",
                authorId = "user2",
                authorName = "ユーザー2",
                commentCount = 0,
                createdAt = "2026-01-21T09:00:00Z"
            )
        )
        fakeNodeRepository.getNodesResult = Result.success(rootOnlyNodes)
        viewModel.loadNodes()

        // When
        val tree = viewModel.getNodeTree()

        // Then
        assertEquals(2, tree.size)
        assertTrue(tree.all { it.depth == 0 })
        assertTrue(tree.all { it.childCount == 0 })
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
