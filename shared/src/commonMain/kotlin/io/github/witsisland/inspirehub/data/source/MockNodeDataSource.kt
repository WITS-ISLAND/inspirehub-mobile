package io.github.witsisland.inspirehub.data.source

import io.github.witsisland.inspirehub.data.dto.NodeDto
import io.github.witsisland.inspirehub.data.dto.ParentNodeDto
import io.github.witsisland.inspirehub.data.dto.ReactionSummaryDto
import io.github.witsisland.inspirehub.data.dto.ReactionsDto
import kotlin.random.Random

class MockNodeDataSource : NodeDataSource {

    private val nodes: MutableList<NodeDto> = mutableListOf()
    private var nextId = 11

    init {
        nodes.addAll(generateMockNodes())
    }

    override suspend fun getNodes(
        type: String?,
        limit: Int,
        offset: Int
    ): List<NodeDto> {
        val filtered = if (type != null) {
            nodes.filter { it.type == type }
        } else {
            nodes.toList()
        }
        return filtered
            .drop(offset)
            .take(limit)
    }

    override suspend fun getNode(id: String): NodeDto {
        return nodes.find { it.id == id }
            ?: throw NoSuchElementException("Node not found: $id")
    }

    override suspend fun createNode(
        title: String,
        content: String,
        type: String,
        tags: List<String>,
        parentNodeId: String?
    ): String {
        val now = "2026-02-01T12:00:00Z"
        val id = "node_${nextId++}"
        val newNode = NodeDto(
            id = id,
            title = title,
            content = content,
            type = type,
            authorId = "user_mock",
            authorName = "テストユーザー",
            authorPicture = null,
            parentNode = null,
            tags = emptyList(),
            reactions = ReactionsDto(),
            commentCount = 0,
            createdAt = now,
            updatedAt = now
        )
        nodes.add(0, newNode)
        return id
    }

    override suspend fun updateNode(
        id: String,
        title: String,
        content: String,
        tags: List<String>
    ): NodeDto {
        val index = nodes.indexOfFirst { it.id == id }
        if (index == -1) throw NoSuchElementException("Node not found: $id")
        val updated = nodes[index].copy(
            title = title,
            content = content,
            updatedAt = "2026-02-01T12:00:00Z"
        )
        nodes[index] = updated
        return updated
    }

    override suspend fun deleteNode(id: String) {
        nodes.removeAll { it.id == id }
    }

    override suspend fun searchNodes(
        query: String,
        type: String?,
        limit: Int,
        offset: Int
    ): List<NodeDto> {
        val lowerQuery = query.lowercase()
        val filtered = nodes.filter { node ->
            val matchesQuery = node.title.lowercase().contains(lowerQuery) ||
                node.content?.lowercase()?.contains(lowerQuery) == true
            val matchesType = type == null || node.type == type
            matchesQuery && matchesType
        }
        return filtered.drop(offset).take(limit)
    }

    override suspend fun getReactedNodes(
        limit: Int,
        offset: Int
    ): List<NodeDto> {
        // Mock: いいね済みのノードを返す
        val reacted = nodes.filter { it.reactions.like.isReacted }
        return reacted.drop(offset).take(limit)
    }

    private fun generateMockNodes(): List<NodeDto> {
        val random = Random(42)

        fun mockReactions(): ReactionsDto {
            return ReactionsDto(
                like = ReactionSummaryDto(count = random.nextInt(31), isReacted = false),
                interested = ReactionSummaryDto(count = random.nextInt(15), isReacted = false),
                wantToTry = ReactionSummaryDto(count = random.nextInt(10), isReacted = false)
            )
        }

        return listOf(
            NodeDto(
                id = "node_1",
                title = "通勤時間の有効活用ができていない",
                content = "毎日の通勤で往復2時間を費やしているが、満員電車でスマホを見る程度しかできていない。この時間をもっと有意義に使いたい。",
                type = "issue",
                authorId = "user_1",
                authorName = "田中太郎",
                tags = emptyList(),
                reactions = mockReactions(),
                commentCount = random.nextInt(31),
                createdAt = "2026-01-20T09:00:00Z",
                updatedAt = "2026-01-20T09:00:00Z"
            ),
            NodeDto(
                id = "node_2",
                title = "地域の高齢者の孤立問題",
                content = "近所に一人暮らしの高齢者が増えている。買い物や病院への移動手段がなく、社会的に孤立しているケースが多い。",
                type = "issue",
                authorId = "user_2",
                authorName = "佐藤花子",
                tags = emptyList(),
                reactions = mockReactions(),
                commentCount = random.nextInt(31),
                createdAt = "2026-01-19T14:30:00Z",
                updatedAt = "2026-01-19T14:30:00Z"
            ),
            NodeDto(
                id = "node_3",
                title = "フードロスが多すぎる",
                content = "スーパーやコンビニで毎日大量の食品が廃棄されている。まだ食べられる食品を必要な人に届ける仕組みが必要。",
                type = "issue",
                authorId = "user_3",
                authorName = "鈴木一郎",
                tags = emptyList(),
                reactions = mockReactions(),
                commentCount = random.nextInt(31),
                createdAt = "2026-01-18T11:00:00Z",
                updatedAt = "2026-01-18T11:00:00Z"
            ),
            NodeDto(
                id = "node_4",
                title = "子どもの学習格差",
                content = "家庭の経済状況によって子どもの学習機会に大きな差が生まれている。塾に通えない子どもたちへのサポートが不足している。",
                type = "issue",
                authorId = "user_4",
                authorName = "高橋美咲",
                tags = emptyList(),
                reactions = mockReactions(),
                commentCount = random.nextInt(31),
                createdAt = "2026-01-17T16:00:00Z",
                updatedAt = "2026-01-17T16:00:00Z"
            ),
            NodeDto(
                id = "node_5",
                title = "リモートワークでのコミュニケーション不足",
                content = "在宅勤務が増えてチーム内の雑談が減った。業務連絡だけでは信頼関係が築きにくく、新人の定着率にも影響が出ている。",
                type = "issue",
                authorId = "user_5",
                authorName = "山田健二",
                tags = emptyList(),
                reactions = mockReactions(),
                commentCount = random.nextInt(31),
                createdAt = "2026-01-16T10:00:00Z",
                updatedAt = "2026-01-16T10:00:00Z"
            ),
            NodeDto(
                id = "node_6",
                title = "音声学習アプリで通勤時間を活用",
                content = "AIが興味関心に合わせて学習コンテンツを音声で読み上げてくれるアプリ。満員電車でも耳だけで学べる。ポッドキャスト形式で5分単位のレッスン。",
                type = "idea",
                authorId = "user_2",
                authorName = "佐藤花子",
                parentNode = ParentNodeDto(id = "node_1", type = "issue", title = "通勤時間の有効活用ができていない", content = "毎日の通勤で往復2時間を費やしているが、満員電車でスマホを見る程度しかできていない。この時間をもっと有意義に使いたい。"),
                tags = emptyList(),
                reactions = mockReactions(),
                commentCount = random.nextInt(31),
                createdAt = "2026-01-21T08:00:00Z",
                updatedAt = "2026-01-21T08:00:00Z"
            ),
            NodeDto(
                id = "node_7",
                title = "シニア向け相乗りマッチングサービス",
                content = "近所の住民同士で買い物や通院の相乗りをマッチングするアプリ。高齢者の移動支援と地域コミュニティの活性化を同時に実現。",
                type = "idea",
                authorId = "user_1",
                authorName = "田中太郎",
                parentNode = ParentNodeDto(id = "node_2", type = "issue", title = "地域の高齢者の孤立問題", content = "近所に一人暮らしの高齢者が増えている。買い物や病院への移動手段がなく、社会的に孤立しているケースが多い。"),
                tags = emptyList(),
                reactions = mockReactions(),
                commentCount = random.nextInt(31),
                createdAt = "2026-01-20T15:00:00Z",
                updatedAt = "2026-01-20T15:00:00Z"
            ),
            NodeDto(
                id = "node_8",
                title = "余剰食品のリアルタイムマッチング",
                content = "店舗の余剰食品をリアルタイムで近隣のフードバンクやNPOに通知・マッチングするプラットフォーム。廃棄前に必要な人へ届ける。",
                type = "idea",
                authorId = "user_3",
                authorName = "鈴木一郎",
                parentNode = ParentNodeDto(id = "node_3", type = "issue", title = "フードロスが多すぎる", content = "スーパーやコンビニで毎日大量の食品が廃棄されている。まだ食べられる食品を必要な人に届ける仕組みが必要。"),
                tags = emptyList(),
                reactions = mockReactions(),
                commentCount = random.nextInt(31),
                createdAt = "2026-01-19T20:00:00Z",
                updatedAt = "2026-01-19T20:00:00Z"
            ),
            NodeDto(
                id = "node_9",
                title = "通勤中にできるマイクロタスクワーク",
                content = "node_1の課題に対する別アプローチ。通勤時間にスマホで完結する短いタスク（翻訳チェック、画像ラベリング等）をこなして副収入を得る仕組み。",
                type = "idea",
                authorId = "user_4",
                authorName = "高橋美咲",
                parentNode = ParentNodeDto(id = "node_1", type = "issue", title = "通勤時間の有効活用ができていない", content = "毎日の通勤で往復2時間を費やしているが、満員電車でスマホを見る程度しかできていない。この時間をもっと有意義に使いたい。"),
                tags = emptyList(),
                reactions = mockReactions(),
                commentCount = random.nextInt(31),
                createdAt = "2026-01-22T07:30:00Z",
                updatedAt = "2026-01-22T07:30:00Z"
            ),
            NodeDto(
                id = "node_10",
                title = "オンライン茶話会プラットフォーム",
                content = "node_2の課題に対して。高齢者向けにワンタップでビデオ通話に参加できる超シンプルなアプリ。地域のボランティアが話し相手として参加。",
                type = "idea",
                authorId = "user_5",
                authorName = "山田健二",
                parentNode = ParentNodeDto(id = "node_2", type = "issue", title = "地域の高齢者の孤立問題", content = "近所に一人暮らしの高齢者が増えている。買い物や病院への移動手段がなく、社会的に孤立しているケースが多い。"),
                tags = emptyList(),
                reactions = mockReactions(),
                commentCount = random.nextInt(31),
                createdAt = "2026-01-21T18:00:00Z",
                updatedAt = "2026-01-21T18:00:00Z"
            )
        )
    }
}
