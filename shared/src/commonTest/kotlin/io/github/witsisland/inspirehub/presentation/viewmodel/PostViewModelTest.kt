package io.github.witsisland.inspirehub.presentation.viewmodel

import io.github.witsisland.inspirehub.domain.model.Node
import io.github.witsisland.inspirehub.domain.model.NodeType
import io.github.witsisland.inspirehub.domain.model.ParentNode
import io.github.witsisland.inspirehub.domain.repository.FakeNodeRepository
import io.github.witsisland.inspirehub.domain.repository.FakeTagRepository
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

class PostViewModelTest : MainDispatcherRule() {

    private lateinit var viewModel: PostViewModel
    private lateinit var fakeNodeRepository: FakeNodeRepository
    private lateinit var fakeTagRepository: FakeTagRepository
    private lateinit var nodeStore: NodeStore

    private val createdNode = Node(
        id = "new_node",
        type = NodeType.ISSUE,
        title = "テスト投稿",
        content = "テスト内容",
        authorId = "user1",
        authorName = "テストユーザー",
        commentCount = 0,
        createdAt = "2026-02-01T09:00:00Z",
        updatedAt = "2026-02-01T09:00:00Z"
    )

    private val parentNode = Node(
        id = "parent1",
        type = NodeType.ISSUE,
        title = "親ノード",
        content = "親の内容",
        authorId = "user2",
        authorName = "親ユーザー",
        commentCount = 1,
        createdAt = "2026-01-20T09:00:00Z",
        updatedAt = "2026-01-20T09:00:00Z"
    )

    @BeforeTest
    fun setup() {
        fakeNodeRepository = FakeNodeRepository()
        fakeTagRepository = FakeTagRepository()
        nodeStore = NodeStore()
        viewModel = PostViewModel(nodeStore, fakeNodeRepository, fakeTagRepository)
    }

    @AfterTest
    fun tearDown() {
        nodeStore.clear()
        fakeNodeRepository.reset()
        fakeTagRepository.reset()
    }

    @Test
    fun `updateTitle - タイトルが更新されること`() = runTest {
        viewModel.updateTitle("新しいタイトル")

        assertEquals("新しいタイトル", viewModel.title.value)
    }

    @Test
    fun `updateContent - コンテンツが更新されること`() = runTest {
        viewModel.updateContent("新しい内容")

        assertEquals("新しい内容", viewModel.content.value)
    }

    @Test
    fun `addTag - タグが追加されること`() = runTest {
        viewModel.addTag("kotlin")
        viewModel.addTag("kmp")

        assertEquals(listOf("kotlin", "kmp"), viewModel.tags.value)
    }

    @Test
    fun `addTag - 重複タグは追加されないこと`() = runTest {
        viewModel.addTag("kotlin")
        viewModel.addTag("kotlin")

        assertEquals(listOf("kotlin"), viewModel.tags.value)
    }

    @Test
    fun `removeTag - タグが削除されること`() = runTest {
        viewModel.addTag("kotlin")
        viewModel.addTag("kmp")

        viewModel.removeTag("kotlin")

        assertEquals(listOf("kmp"), viewModel.tags.value)
    }

    @Test
    fun `setParentNode - 親ノードが設定されること`() = runTest {
        viewModel.setParentNode(parentNode)

        assertEquals(parentNode, viewModel.parentNode.value)
    }

    @Test
    fun `setParentNode - nullで親ノードがクリアされること`() = runTest {
        viewModel.setParentNode(parentNode)

        viewModel.setParentNode(null)

        assertNull(viewModel.parentNode.value)
    }

    @Test
    fun `submitIssue - 課題投稿が成功すること`() = runTest {
        viewModel.updateTitle("テスト課題")
        viewModel.updateContent("課題の内容")
        viewModel.addTag("backend")
        fakeNodeRepository.createNodeResult = Result.success(createdNode)

        viewModel.submitIssue()

        assertEquals(1, fakeNodeRepository.createNodeCallCount)
        assertEquals("テスト課題", fakeNodeRepository.lastCreateNodeTitle)
        assertEquals("課題の内容", fakeNodeRepository.lastCreateNodeContent)
        assertEquals(NodeType.ISSUE, fakeNodeRepository.lastCreateNodeType)
        assertNull(fakeNodeRepository.lastCreateNodeParentNodeId)
        assertEquals(listOf("backend"), fakeNodeRepository.lastCreateNodeTags)
        assertTrue(viewModel.isSuccess.value)
        assertFalse(viewModel.isSubmitting.value)
        assertNull(viewModel.error.value)
    }

    @Test
    fun `submitIssue - 失敗時にエラーが設定されること`() = runTest {
        viewModel.updateTitle("テスト")
        viewModel.updateContent("内容")
        val errorMessage = "Create failed"
        fakeNodeRepository.createNodeResult = Result.failure(Exception(errorMessage))

        viewModel.submitIssue()

        assertEquals(errorMessage, viewModel.error.value)
        assertFalse(viewModel.isSuccess.value)
        assertFalse(viewModel.isSubmitting.value)
    }

    @Test
    fun `submitIdea - アイデア投稿が成功すること`() = runTest {
        viewModel.updateTitle("テストアイデア")
        viewModel.updateContent("アイデアの内容")
        val ideaNode = createdNode.copy(type = NodeType.IDEA)
        fakeNodeRepository.createNodeResult = Result.success(ideaNode)

        viewModel.submitIdea()

        assertEquals(NodeType.IDEA, fakeNodeRepository.lastCreateNodeType)
        assertNull(fakeNodeRepository.lastCreateNodeParentNodeId)
        assertTrue(viewModel.isSuccess.value)
    }

    @Test
    fun `submitDerived - 派生投稿が成功すること`() = runTest {
        viewModel.setParentNode(parentNode)
        viewModel.updateTitle("派生アイデア")
        viewModel.updateContent("派生の内容")
        val derivedNode = createdNode.copy(
            type = NodeType.IDEA,
            parentNode = ParentNode(id = "parent1", type = NodeType.ISSUE, title = "親ノード")
        )
        fakeNodeRepository.createNodeResult = Result.success(derivedNode)

        viewModel.submitDerived()

        assertEquals(NodeType.IDEA, fakeNodeRepository.lastCreateNodeType)
        assertEquals("parent1", fakeNodeRepository.lastCreateNodeParentNodeId)
        assertTrue(viewModel.isSuccess.value)
    }

    @Test
    fun `submitDerived - 親ノード未設定でもparentNodeIdがnullで投稿されること`() = runTest {
        viewModel.updateTitle("テスト")
        viewModel.updateContent("内容")
        fakeNodeRepository.createNodeResult = Result.success(createdNode)

        viewModel.submitDerived()

        assertNull(fakeNodeRepository.lastCreateNodeParentNodeId)
    }

    @Test
    fun `submit成功 - NodeStoreにノードが追加されること`() = runTest {
        viewModel.updateTitle("テスト")
        viewModel.updateContent("内容")
        fakeNodeRepository.createNodeResult = Result.success(createdNode)

        viewModel.submitIssue()

        assertTrue(nodeStore.nodes.value.any { it.id == "new_node" })
    }

    @Test
    fun `reset - フォームがクリアされること`() = runTest {
        viewModel.updateTitle("タイトル")
        viewModel.updateContent("内容")
        viewModel.addTag("tag1")
        viewModel.setParentNode(parentNode)

        viewModel.reset()

        assertEquals("", viewModel.title.value)
        assertEquals("", viewModel.content.value)
        assertTrue(viewModel.tags.value.isEmpty())
        assertNull(viewModel.parentNode.value)
        assertFalse(viewModel.isSubmitting.value)
        assertNull(viewModel.error.value)
        assertFalse(viewModel.isSuccess.value)
    }

    @Test
    fun `初期状態 - フォームが空であること`() = runTest {
        assertEquals("", viewModel.title.value)
        assertEquals("", viewModel.content.value)
        assertTrue(viewModel.tags.value.isEmpty())
        assertNull(viewModel.parentNode.value)
        assertFalse(viewModel.isSubmitting.value)
        assertNull(viewModel.error.value)
        assertFalse(viewModel.isSuccess.value)
        assertFalse(viewModel.isValid.value)
    }

    // ========================================
    // isValid バリデーション (BUG-010)
    // ========================================

    @Test
    fun `isValid - タイトルと本文の両方が入力されている場合trueになること`() = runTest {
        viewModel.updateTitle("タイトル")
        viewModel.updateContent("本文")

        assertTrue(viewModel.isValid.value)
    }

    @Test
    fun `isValid - タイトルのみの場合falseであること`() = runTest {
        viewModel.updateTitle("タイトル")

        assertFalse(viewModel.isValid.value)
    }

    @Test
    fun `isValid - 本文のみの場合falseであること`() = runTest {
        viewModel.updateContent("本文")

        assertFalse(viewModel.isValid.value)
    }

    @Test
    fun `isValid - 空白のみの場合falseであること`() = runTest {
        viewModel.updateTitle("  ")
        viewModel.updateContent("  ")

        assertFalse(viewModel.isValid.value)
    }

    @Test
    fun `isValid - resetでfalseに戻ること`() = runTest {
        viewModel.updateTitle("タイトル")
        viewModel.updateContent("本文")
        assertTrue(viewModel.isValid.value)

        viewModel.reset()

        assertFalse(viewModel.isValid.value)
    }
}
