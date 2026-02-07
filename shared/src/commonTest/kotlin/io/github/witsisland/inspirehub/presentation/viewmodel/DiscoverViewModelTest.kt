package io.github.witsisland.inspirehub.presentation.viewmodel

import app.cash.turbine.test
import io.github.witsisland.inspirehub.domain.model.Node
import io.github.witsisland.inspirehub.domain.model.NodeType
import io.github.witsisland.inspirehub.domain.model.Reactions
import io.github.witsisland.inspirehub.domain.model.ReactionSummary
import io.github.witsisland.inspirehub.domain.model.Tag
import io.github.witsisland.inspirehub.domain.repository.FakeNodeRepository
import io.github.witsisland.inspirehub.domain.repository.FakeTagRepository
import io.github.witsisland.inspirehub.domain.store.DiscoverStore
import io.github.witsisland.inspirehub.test.MainDispatcherRule
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DiscoverViewModelTest : MainDispatcherRule() {

    private lateinit var viewModel: DiscoverViewModel
    private lateinit var fakeNodeRepository: FakeNodeRepository
    private lateinit var fakeTagRepository: FakeTagRepository
    private lateinit var discoverStore: DiscoverStore

    private val sampleNodes = listOf(
        Node(
            id = "node1",
            type = NodeType.ISSUE,
            title = "検索テスト課題",
            content = "検索対象の内容",
            authorId = "user1",
            authorName = "テストユーザー1",
            reactions = Reactions(
                like = ReactionSummary(count = 10),
                interested = ReactionSummary(count = 5),
                wantToTry = ReactionSummary(count = 3)
            ),
            commentCount = 2,
            createdAt = "2026-01-20T09:00:00Z",
            updatedAt = "2026-01-20T09:00:00Z"
        ),
        Node(
            id = "node2",
            type = NodeType.IDEA,
            title = "検索テストアイデア",
            content = "アイデアの内容",
            authorId = "user2",
            authorName = "テストユーザー2",
            reactions = Reactions(
                like = ReactionSummary(count = 20),
                interested = ReactionSummary(count = 10),
                wantToTry = ReactionSummary(count = 8)
            ),
            commentCount = 5,
            createdAt = "2026-01-21T09:00:00Z",
            updatedAt = "2026-01-21T09:00:00Z"
        )
    )

    private val sampleTags = listOf(
        Tag(id = "tag1", name = "AI", usageCount = 50),
        Tag(id = "tag2", name = "モバイル", usageCount = 30),
        Tag(id = "tag3", name = "教育", usageCount = 20)
    )

    @BeforeTest
    fun setup() {
        fakeNodeRepository = FakeNodeRepository()
        fakeTagRepository = FakeTagRepository()
        discoverStore = DiscoverStore()
        viewModel = DiscoverViewModel(discoverStore, fakeNodeRepository, fakeTagRepository)
    }

    @AfterTest
    fun tearDown() {
        discoverStore.clear()
        fakeNodeRepository.reset()
        fakeTagRepository.reset()
    }

    @Test
    fun `search - 検索が成功すること`() = runTest {
        fakeNodeRepository.searchNodesResult = Result.success(sampleNodes)

        viewModel.search("テスト")
        advanceUntilIdle()

        assertEquals(1, fakeNodeRepository.searchNodesCallCount)
        assertEquals("テスト", fakeNodeRepository.lastSearchQuery)
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.error.value)
        assertEquals(sampleNodes.size, viewModel.searchResults.value.size)
    }

    @Test
    fun `search - 空クエリで検索結果がクリアされること`() = runTest {
        discoverStore.updateSearchResults(sampleNodes)

        viewModel.search("")

        assertEquals(0, fakeNodeRepository.searchNodesCallCount)
        assertTrue(viewModel.searchResults.value.isEmpty())
    }

    @Test
    fun `search - 空白のみのクエリで検索結果がクリアされること`() = runTest {
        discoverStore.updateSearchResults(sampleNodes)

        viewModel.search("   ")

        assertEquals(0, fakeNodeRepository.searchNodesCallCount)
        assertTrue(viewModel.searchResults.value.isEmpty())
    }

    @Test
    fun `search - 失敗時にエラーが設定されること`() = runTest {
        val errorMessage = "Search API error"
        fakeNodeRepository.searchNodesResult = Result.failure(Exception(errorMessage))

        viewModel.search("テスト")
        advanceUntilIdle()

        assertEquals(errorMessage, viewModel.error.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `search - searchQueryが更新されること`() = runTest {
        fakeNodeRepository.searchNodesResult = Result.success(emptyList())

        viewModel.search("クエリテスト")
        advanceUntilIdle()

        viewModel.searchQuery.test {
            assertEquals("クエリテスト", awaitItem())
        }
    }

    @Test
    fun `loadPopularTags - 人気タグの取得が成功すること`() = runTest {
        fakeTagRepository.getPopularTagsResult = Result.success(sampleTags)

        viewModel.loadPopularTags()

        assertEquals(1, fakeTagRepository.getPopularTagsCallCount)
        assertNull(viewModel.error.value)
        assertEquals(sampleTags.size, viewModel.popularTags.value.size)
        assertEquals("AI", viewModel.popularTags.value[0].name)
    }

    @Test
    fun `loadPopularTags - 失敗時にエラーが設定されること`() = runTest {
        val errorMessage = "Tags API error"
        fakeTagRepository.getPopularTagsResult = Result.failure(Exception(errorMessage))

        viewModel.loadPopularTags()

        assertEquals(errorMessage, viewModel.error.value)
    }

    @Test
    fun `loadPopularNodes - 人気ノードがリアクション数降順でソートされること`() = runTest {
        fakeNodeRepository.getNodesResult = Result.success(sampleNodes)

        viewModel.loadPopularNodes()

        assertEquals(1, fakeNodeRepository.getNodesCallCount)
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.error.value)

        val popularNodes = viewModel.popularNodes.value
        assertEquals(2, popularNodes.size)
        assertEquals("node2", popularNodes[0].id)
        assertEquals("node1", popularNodes[1].id)
    }

    @Test
    fun `loadPopularNodes - 失敗時にエラーが設定されること`() = runTest {
        val errorMessage = "Nodes API error"
        fakeNodeRepository.getNodesResult = Result.failure(Exception(errorMessage))

        viewModel.loadPopularNodes()

        assertEquals(errorMessage, viewModel.error.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `selectTag - タグ名で検索が実行されること`() = runTest {
        fakeNodeRepository.searchNodesResult = Result.success(sampleNodes)
        val tag = Tag(id = "tag1", name = "AI", usageCount = 50)

        viewModel.selectTag(tag)
        advanceUntilIdle()

        assertEquals(1, fakeNodeRepository.searchNodesCallCount)
        assertEquals("AI", fakeNodeRepository.lastSearchQuery)
        viewModel.searchQuery.test {
            assertEquals("AI", awaitItem())
        }
    }

    @Test
    fun `初期状態 - 全てが空またはデフォルトであること`() = runTest {
        assertEquals("", viewModel.searchQuery.value)
        assertTrue(viewModel.searchResults.value.isEmpty())
        assertTrue(viewModel.popularTags.value.isEmpty())
        assertTrue(viewModel.popularNodes.value.isEmpty())
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.error.value)
    }

    @Test
    fun `search - DiscoverStoreに結果が反映されること`() = runTest {
        fakeNodeRepository.searchNodesResult = Result.success(sampleNodes)

        viewModel.search("テスト")
        advanceUntilIdle()

        assertEquals(sampleNodes, discoverStore.searchResults.value)
    }

    @Test
    fun `loadPopularTags - DiscoverStoreに結果が反映されること`() = runTest {
        fakeTagRepository.getPopularTagsResult = Result.success(sampleTags)

        viewModel.loadPopularTags()

        assertEquals(sampleTags, discoverStore.popularTags.value)
    }

    @Test
    fun `loadPopularNodes - DiscoverStoreに結果が反映されること`() = runTest {
        fakeNodeRepository.getNodesResult = Result.success(sampleNodes)

        viewModel.loadPopularNodes()

        val storeNodes = discoverStore.popularNodes.value
        assertEquals(2, storeNodes.size)
        assertEquals("node2", storeNodes[0].id)
    }
}
