package io.github.witsisland.inspirehub.presentation.viewmodel

import app.cash.turbine.test
import io.github.witsisland.inspirehub.domain.model.Comment
import io.github.witsisland.inspirehub.domain.model.Node
import io.github.witsisland.inspirehub.domain.model.NodeType
import io.github.witsisland.inspirehub.domain.model.ParentNode
import io.github.witsisland.inspirehub.domain.model.Reactions
import io.github.witsisland.inspirehub.domain.model.ReactionSummary
import io.github.witsisland.inspirehub.domain.model.ReactionType
import io.github.witsisland.inspirehub.domain.repository.FakeCommentRepository
import io.github.witsisland.inspirehub.domain.repository.FakeNodeRepository
import io.github.witsisland.inspirehub.domain.repository.FakeReactionRepository
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

class DetailViewModelTest : MainDispatcherRule() {

    private lateinit var viewModel: DetailViewModel
    private lateinit var fakeNodeRepository: FakeNodeRepository
    private lateinit var fakeCommentRepository: FakeCommentRepository
    private lateinit var fakeReactionRepository: FakeReactionRepository
    private lateinit var nodeStore: NodeStore

    private val sampleNode = Node(
        id = "node1",
        type = NodeType.ISSUE,
        title = "テスト課題",
        content = "課題の詳細内容",
        authorId = "user1",
        authorName = "テストユーザー",
        reactions = Reactions(like = ReactionSummary(count = 5)),
        commentCount = 2,
        createdAt = "2026-01-20T09:00:00Z",
        updatedAt = "2026-01-20T09:00:00Z"
    )

    private val sampleComments = listOf(
        Comment(
            id = "comment1",
            nodeId = "node1",
            parentId = null,
            authorId = "user2",
            authorName = "コメントユーザー1",
            content = "コメント1",
            createdAt = "2026-01-20T10:00:00Z"
        ),
        Comment(
            id = "comment2",
            nodeId = "node1",
            parentId = null,
            authorId = "user3",
            authorName = "コメントユーザー2",
            content = "コメント2",
            createdAt = "2026-01-20T11:00:00Z"
        )
    )

    private val sampleChildNodes = listOf(
        Node(
            id = "child1",
            type = NodeType.IDEA,
            title = "派生アイデア",
            content = "派生の内容",
            authorId = "user2",
            authorName = "派生ユーザー",
            parentNode = ParentNode(id = "node1", type = NodeType.ISSUE, title = "テスト課題"),
            reactions = Reactions(like = ReactionSummary(count = 3)),
            commentCount = 0,
            createdAt = "2026-01-21T09:00:00Z",
            updatedAt = "2026-01-21T09:00:00Z"
        )
    )

    @BeforeTest
    fun setup() {
        fakeNodeRepository = FakeNodeRepository()
        fakeCommentRepository = FakeCommentRepository()
        fakeReactionRepository = FakeReactionRepository()
        nodeStore = NodeStore()
        viewModel = DetailViewModel(nodeStore, fakeNodeRepository, fakeCommentRepository, fakeReactionRepository)
    }

    @AfterTest
    fun tearDown() {
        nodeStore.clear()
        fakeNodeRepository.reset()
        fakeCommentRepository.reset()
        fakeReactionRepository.reset()
    }

    @Test
    fun `loadDetail - ノード詳細とコメントと子ノードが取得されること`() = runTest {
        fakeNodeRepository.getNodeResult = Result.success(sampleNode)
        fakeCommentRepository.getCommentsResult = Result.success(sampleComments)
        fakeNodeRepository.getChildNodesResult = Result.success(sampleChildNodes)

        viewModel.loadDetail("node1")

        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.error.value)
        assertEquals(1, fakeNodeRepository.getNodeCallCount)
        assertEquals(1, fakeCommentRepository.getCommentsCallCount)
        assertEquals(1, fakeNodeRepository.getChildNodesCallCount)

        viewModel.selectedNode.test {
            assertEquals(sampleNode, awaitItem())
        }
        assertEquals(sampleComments.size, viewModel.comments.value.size)
        assertEquals(sampleChildNodes.size, viewModel.childNodes.value.size)
    }

    @Test
    fun `loadDetail - ノード取得失敗時にエラーが設定されること`() = runTest {
        val errorMessage = "Node not found"
        fakeNodeRepository.getNodeResult = Result.failure(Exception(errorMessage))
        fakeCommentRepository.getCommentsResult = Result.success(emptyList())
        fakeNodeRepository.getChildNodesResult = Result.success(emptyList())

        viewModel.loadDetail("nonexistent")

        assertEquals(errorMessage, viewModel.error.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `loadDetail - コメント取得失敗でもノードは表示されること`() = runTest {
        fakeNodeRepository.getNodeResult = Result.success(sampleNode)
        fakeCommentRepository.getCommentsResult = Result.failure(Exception("Comment fetch failed"))
        fakeNodeRepository.getChildNodesResult = Result.success(emptyList())

        viewModel.loadDetail("node1")

        viewModel.selectedNode.test {
            assertEquals(sampleNode, awaitItem())
        }
        assertTrue(viewModel.comments.value.isEmpty())
    }

    @Test
    fun `loadDetail - 子ノード取得失敗でもノードとコメントは表示されること`() = runTest {
        fakeNodeRepository.getNodeResult = Result.success(sampleNode)
        fakeCommentRepository.getCommentsResult = Result.success(sampleComments)
        fakeNodeRepository.getChildNodesResult = Result.failure(Exception("Child nodes fetch failed"))

        viewModel.loadDetail("node1")

        viewModel.selectedNode.test {
            assertEquals(sampleNode, awaitItem())
        }
        assertEquals(sampleComments.size, viewModel.comments.value.size)
        assertTrue(viewModel.childNodes.value.isEmpty())
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `toggleReaction - 楽観的更新でUI即座反映されること`() = runTest {
        nodeStore.selectNode(sampleNode)
        fakeReactionRepository.toggleReactionResult = Result.success(
            ReactionSummary(count = 6, isReacted = true)
        )

        viewModel.toggleReaction(ReactionType.LIKE)

        // 楽観的更新でcount+1, isReacted=trueになっている
        viewModel.selectedNode.test {
            val node = awaitItem()!!
            assertEquals(6, node.reactions.like.count)
            assertTrue(node.reactions.like.isReacted)
        }

        assertEquals(1, fakeReactionRepository.toggleReactionCallCount)
        assertEquals("node1", fakeReactionRepository.lastToggleReactionNodeId)
        assertEquals(ReactionType.LIKE, fakeReactionRepository.lastToggleReactionType)
        assertNull(viewModel.error.value)
    }

    @Test
    fun `toggleReaction - 選択ノードがない場合は何もしないこと`() = runTest {
        viewModel.toggleReaction(ReactionType.LIKE)

        assertEquals(0, fakeReactionRepository.toggleReactionCallCount)
    }

    @Test
    fun `toggleReaction - 失敗時にロールバックされること`() = runTest {
        nodeStore.selectNode(sampleNode)
        val errorMessage = "Reaction failed"
        fakeReactionRepository.toggleReactionResult = Result.failure(Exception(errorMessage))

        viewModel.toggleReaction(ReactionType.WANT_TO_TRY)

        // 失敗時はロールバック（元の値に戻る）
        viewModel.selectedNode.test {
            val node = awaitItem()!!
            assertEquals(0, node.reactions.wantToTry.count)
            assertFalse(node.reactions.wantToTry.isReacted)
        }
        assertEquals(errorMessage, viewModel.error.value)
    }

    @Test
    fun `updateCommentText - コメントテキストが更新されること`() = runTest {
        viewModel.updateCommentText("テストコメント")

        assertEquals("テストコメント", viewModel.commentText.value)
    }

    @Test
    fun `submitComment - コメント投稿が成功すること`() = runTest {
        nodeStore.selectNode(sampleNode)
        viewModel.updateCommentText("新しいコメント")
        val newComment = Comment(
            id = "comment_new",
            nodeId = "node1",
            parentId = null,
            authorId = "user1",
            authorName = "投稿ユーザー",
            content = "新しいコメント",
            createdAt = "2026-01-22T09:00:00Z"
        )
        fakeCommentRepository.createCommentResult = Result.success("comment_new")
        // submitComment成功後にgetCommentsが呼ばれるので、再取得結果を設定
        fakeCommentRepository.comments.add(newComment)

        viewModel.submitComment()

        assertEquals(1, fakeCommentRepository.createCommentCallCount)
        assertEquals("node1", fakeCommentRepository.lastCreateCommentNodeId)
        assertEquals("新しいコメント", fakeCommentRepository.lastCreateCommentContent)
        assertNull(fakeCommentRepository.lastCreateCommentParentId)
        assertFalse(viewModel.isCommentSubmitting.value)
        assertEquals("", viewModel.commentText.value)
        assertTrue(viewModel.comments.value.any { it.id == "comment_new" })
    }

    @Test
    fun `submitComment - 空のテキストでは投稿しないこと`() = runTest {
        nodeStore.selectNode(sampleNode)
        viewModel.updateCommentText("   ")

        viewModel.submitComment()

        assertEquals(0, fakeCommentRepository.createCommentCallCount)
    }

    @Test
    fun `submitComment - ノード未選択では投稿しないこと`() = runTest {
        viewModel.updateCommentText("コメント")

        viewModel.submitComment()

        assertEquals(0, fakeCommentRepository.createCommentCallCount)
    }

    @Test
    fun `submitComment - 失敗時にエラーが設定されテキストが復元されること`() = runTest {
        nodeStore.selectNode(sampleNode)
        viewModel.updateCommentText("テストコメント")
        val errorMessage = "Comment post failed"
        fakeCommentRepository.createCommentResult = Result.failure(Exception(errorMessage))

        viewModel.submitComment()

        assertEquals(errorMessage, viewModel.error.value)
        assertFalse(viewModel.isCommentSubmitting.value)
        // 楽観的更新の失敗時: テキストが復元され、楽観的コメントが除去される
        assertEquals("テストコメント", viewModel.commentText.value)
        assertFalse(viewModel.comments.value.any { it.content == "テストコメント" })
    }

    @Test
    fun `selectNode - NodeStoreに選択が反映されること`() = runTest {
        viewModel.selectNode(sampleNode)

        viewModel.selectedNode.test {
            assertEquals(sampleNode, awaitItem())
        }
    }

    @Test
    fun `初期状態 - 全てが空またはnullであること`() = runTest {
        assertNull(viewModel.selectedNode.value)
        assertTrue(viewModel.comments.value.isEmpty())
        assertTrue(viewModel.childNodes.value.isEmpty())
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.error.value)
        assertEquals("", viewModel.commentText.value)
        assertFalse(viewModel.isCommentSubmitting.value)
        assertFalse(viewModel.isEditing.value)
        assertFalse(viewModel.isDeleted.value)
    }

    // --- 編集機能のテスト ---

    @Test
    fun `startEditing - 編集モードが開始されタイトルと内容がセットされること`() = runTest {
        nodeStore.selectNode(sampleNode)

        viewModel.startEditing()

        assertTrue(viewModel.isEditing.value)
        assertEquals(sampleNode.title, viewModel.editTitle.value)
        assertEquals(sampleNode.content, viewModel.editContent.value)
    }

    @Test
    fun `startEditing - 選択ノードがない場合は編集モードにならないこと`() = runTest {
        viewModel.startEditing()

        assertFalse(viewModel.isEditing.value)
    }

    @Test
    fun `cancelEditing - 編集モードがキャンセルされること`() = runTest {
        nodeStore.selectNode(sampleNode)
        viewModel.startEditing()

        viewModel.cancelEditing()

        assertFalse(viewModel.isEditing.value)
        assertEquals("", viewModel.editTitle.value)
        assertEquals("", viewModel.editContent.value)
    }

    @Test
    fun `updateEditTitle - 編集タイトルが更新されること`() = runTest {
        viewModel.updateEditTitle("新しいタイトル")

        assertEquals("新しいタイトル", viewModel.editTitle.value)
    }

    @Test
    fun `updateEditContent - 編集内容が更新されること`() = runTest {
        viewModel.updateEditContent("新しい内容")

        assertEquals("新しい内容", viewModel.editContent.value)
    }

    @Test
    fun `saveEdit - 編集保存が成功すること`() = runTest {
        nodeStore.selectNode(sampleNode)
        viewModel.startEditing()
        viewModel.updateEditTitle("更新後のタイトル")
        viewModel.updateEditContent("更新後の内容")

        val updatedNode = sampleNode.copy(
            title = "更新後のタイトル",
            content = "更新後の内容"
        )
        fakeNodeRepository.updateNodeResult = Result.success(updatedNode)

        viewModel.saveEdit()

        assertEquals(1, fakeNodeRepository.updateNodeCallCount)
        assertEquals("node1", fakeNodeRepository.lastUpdateNodeId)
        assertEquals("更新後のタイトル", fakeNodeRepository.lastUpdateNodeTitle)
        assertEquals("更新後の内容", fakeNodeRepository.lastUpdateNodeContent)
        assertFalse(viewModel.isEditing.value)
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.error.value)
        viewModel.selectedNode.test {
            assertEquals(updatedNode, awaitItem())
        }
    }

    @Test
    fun `saveEdit - タイトルが空の場合は保存しないこと`() = runTest {
        nodeStore.selectNode(sampleNode)
        viewModel.startEditing()
        viewModel.updateEditTitle("   ")

        viewModel.saveEdit()

        assertEquals(0, fakeNodeRepository.updateNodeCallCount)
    }

    @Test
    fun `saveEdit - 失敗時にエラーが設定されること`() = runTest {
        nodeStore.selectNode(sampleNode)
        viewModel.startEditing()
        viewModel.updateEditTitle("更新タイトル")
        viewModel.updateEditContent("更新内容")

        val errorMessage = "Update failed"
        fakeNodeRepository.updateNodeResult = Result.failure(Exception(errorMessage))

        viewModel.saveEdit()

        assertEquals(errorMessage, viewModel.error.value)
        assertTrue(viewModel.isEditing.value) // 編集モードは維持
        assertFalse(viewModel.isLoading.value)
    }

    // --- 削除機能のテスト ---

    @Test
    fun `deleteNode - 削除が成功すること`() = runTest {
        nodeStore.selectNode(sampleNode)
        fakeNodeRepository.deleteNodeResult = Result.success(Unit)

        viewModel.deleteNode()

        assertEquals(1, fakeNodeRepository.deleteNodeCallCount)
        assertEquals("node1", fakeNodeRepository.lastDeleteNodeId)
        assertTrue(viewModel.isDeleted.value)
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.error.value)
    }

    @Test
    fun `deleteNode - 選択ノードがない場合は何もしないこと`() = runTest {
        viewModel.deleteNode()

        assertEquals(0, fakeNodeRepository.deleteNodeCallCount)
    }

    @Test
    fun `deleteNode - 失敗時にエラーが設定されること`() = runTest {
        nodeStore.selectNode(sampleNode)
        val errorMessage = "Delete failed"
        fakeNodeRepository.deleteNodeResult = Result.failure(Exception(errorMessage))

        viewModel.deleteNode()

        assertEquals(errorMessage, viewModel.error.value)
        assertFalse(viewModel.isDeleted.value)
        assertFalse(viewModel.isLoading.value)
    }

    // --- コメント編集機能のテスト ---

    @Test
    fun `startEditingComment - 編集モードが開始されコメント内容がセットされること`() = runTest {
        val comment = sampleComments[0]

        viewModel.startEditingComment(comment = comment)

        assertEquals(comment.id, viewModel.editingCommentId.value)
        assertEquals(comment.content, viewModel.editCommentText.value)
    }

    @Test
    fun `cancelEditingComment - 編集モードがキャンセルされること`() = runTest {
        viewModel.startEditingComment(comment = sampleComments[0])

        viewModel.cancelEditingComment()

        assertNull(viewModel.editingCommentId.value)
        assertEquals("", viewModel.editCommentText.value)
    }

    @Test
    fun `saveCommentEdit - コメント編集が成功すること`() = runTest {
        // コメント一覧をセット
        fakeNodeRepository.getNodeResult = Result.success(sampleNode)
        fakeCommentRepository.getCommentsResult = Result.success(sampleComments)
        fakeNodeRepository.getChildNodesResult = Result.success(emptyList())
        viewModel.loadDetail("node1")

        val targetComment = sampleComments[0]
        viewModel.startEditingComment(comment = targetComment)
        viewModel.updateEditCommentText(text = "更新後のコメント")

        fakeCommentRepository.updateCommentResult = Result.success(Unit)

        viewModel.saveCommentEdit()

        assertEquals(1, fakeCommentRepository.updateCommentCallCount)
        assertEquals(targetComment.id, fakeCommentRepository.lastUpdateCommentId)
        assertEquals("更新後のコメント", fakeCommentRepository.lastUpdateCommentContent)
        assertNull(viewModel.editingCommentId.value)
        assertEquals("", viewModel.editCommentText.value)
        assertEquals("更新後のコメント", viewModel.comments.value.first { it.id == targetComment.id }.content)
    }

    @Test
    fun `saveCommentEdit - 空テキストでは保存しないこと`() = runTest {
        viewModel.startEditingComment(comment = sampleComments[0])
        viewModel.updateEditCommentText(text = "   ")

        viewModel.saveCommentEdit()

        assertEquals(0, fakeCommentRepository.updateCommentCallCount)
    }

    @Test
    fun `saveCommentEdit - 失敗時にエラーが設定されること`() = runTest {
        viewModel.startEditingComment(comment = sampleComments[0])
        viewModel.updateEditCommentText(text = "更新テスト")

        val errorMessage = "Update comment failed"
        fakeCommentRepository.updateCommentResult = Result.failure(Exception(errorMessage))

        viewModel.saveCommentEdit()

        assertEquals(errorMessage, viewModel.error.value)
        assertEquals(sampleComments[0].id, viewModel.editingCommentId.value) // 編集モード維持
    }

    // --- コメント削除機能のテスト ---

    @Test
    fun `deleteComment - コメント削除が成功すること`() = runTest {
        // コメント一覧をセット
        fakeNodeRepository.getNodeResult = Result.success(sampleNode)
        fakeCommentRepository.getCommentsResult = Result.success(sampleComments)
        fakeNodeRepository.getChildNodesResult = Result.success(emptyList())
        viewModel.loadDetail("node1")

        assertEquals(2, viewModel.comments.value.size)

        fakeCommentRepository.deleteCommentResult = Result.success(Unit)

        viewModel.deleteComment(commentId = "comment1")

        assertEquals(1, fakeCommentRepository.deleteCommentCallCount)
        assertEquals("comment1", fakeCommentRepository.lastDeleteCommentId)
        assertEquals(1, viewModel.comments.value.size)
        assertFalse(viewModel.comments.value.any { it.id == "comment1" })
        assertNull(viewModel.error.value)
    }

    @Test
    fun `deleteComment - 失敗時にエラーが設定されること`() = runTest {
        // コメント一覧をセット
        fakeNodeRepository.getNodeResult = Result.success(sampleNode)
        fakeCommentRepository.getCommentsResult = Result.success(sampleComments)
        fakeNodeRepository.getChildNodesResult = Result.success(emptyList())
        viewModel.loadDetail("node1")

        val errorMessage = "Delete comment failed"
        fakeCommentRepository.deleteCommentResult = Result.failure(Exception(errorMessage))

        viewModel.deleteComment(commentId = "comment1")

        assertEquals(errorMessage, viewModel.error.value)
        assertEquals(2, viewModel.comments.value.size) // 削除されない
    }
}
