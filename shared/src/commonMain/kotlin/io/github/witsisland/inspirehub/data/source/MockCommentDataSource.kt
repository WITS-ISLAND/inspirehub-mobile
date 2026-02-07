package io.github.witsisland.inspirehub.data.source

import io.github.witsisland.inspirehub.data.dto.CommentDto

class MockCommentDataSource : CommentDataSource {

    private val comments: MutableList<CommentDto> = mutableListOf()
    private var nextId = 100

    init {
        generateSampleComments()
    }

    override suspend fun getComments(nodeId: String): List<CommentDto> {
        return comments.filter { it.nodeId == nodeId }
    }

    override suspend fun getComment(id: String): CommentDto {
        return comments.first { it.id == id }
    }

    override suspend fun createComment(
        nodeId: String,
        content: String,
        parentId: String?
    ): CommentDto {
        val newComment = CommentDto(
            id = "comment_${nextId++}",
            content = content,
            authorId = "user_mock",
            authorName = "テストユーザー",
            authorPicture = null,
            nodeId = nodeId,
            parentId = parentId,
            mentions = emptyList(),
            replies = emptyList(),
            createdAt = "2026-02-02T00:00:00Z",
            updatedAt = "2026-02-02T00:00:00Z"
        )
        comments.add(newComment)
        return newComment
    }

    override suspend fun updateComment(id: String, content: String): CommentDto {
        val index = comments.indexOfFirst { it.id == id }
        val updated = comments[index].copy(
            content = content,
            updatedAt = "2026-02-02T00:00:01Z"
        )
        comments[index] = updated
        return updated
    }

    override suspend fun deleteComment(id: String) {
        comments.removeAll { it.id == id }
    }

    private fun generateSampleComments() {
        val authors = listOf(
            Triple("user_1", "鈴木一郎", null as String?),
            Triple("user_2", "高橋美咲", null),
            Triple("user_3", "田中健太", null),
            Triple("user_4", "佐藤花子", null),
            Triple("user_5", "山田太郎", null)
        )

        val sampleContents = listOf(
            "面白いアイデアですね！ぜひ深掘りしましょう。",
            "この方向性で進めるのが良さそうです。",
            "少し気になる点があります。後で詳しく話せますか？",
            "素晴らしい整理ですね。全体像が見えやすくなりました。",
            "ここはもう少し具体的な数値があると説得力が増しますね。",
            "前回のミーティングで話した内容と合致しています。",
            "別のアプローチも検討してみてはどうでしょうか。",
            "参考資料を共有します。確認お願いします。",
            "締め切りまでに対応可能です。",
            "優先度を上げたほうが良いかもしれません。"
        )

        val replyContents = listOf(
            "なるほど、確かにそうですね。",
            "承知しました。対応します。",
            "補足ありがとうございます。参考になります。",
            "同意です。その方向で進めましょう。"
        )

        var commentId = 1

        for (nodeIndex in 1..10) {
            val nodeId = "node_$nodeIndex"
            val numComments = if (nodeIndex % 3 == 0) 3 else 2

            for (i in 0 until numComments) {
                val author = authors[(commentId - 1) % authors.size]
                val content = sampleContents[(commentId - 1) % sampleContents.size]
                val parentCommentId = "comment_$commentId"

                comments.add(
                    CommentDto(
                        id = parentCommentId,
                        content = content,
                        authorId = author.first,
                        authorName = author.second,
                        authorPicture = author.third,
                        nodeId = nodeId,
                        parentId = null,
                        mentions = emptyList(),
                        replies = emptyList(),
                        createdAt = "2026-01-${((commentId % 28) + 1).toString().padStart(2, '0')}T09:00:00Z",
                        updatedAt = "2026-01-${((commentId % 28) + 1).toString().padStart(2, '0')}T09:00:00Z"
                    )
                )
                commentId++

                if (i == 0 && nodeIndex % 2 == 0) {
                    val replyAuthor = authors[commentId % authors.size]
                    val replyContent = replyContents[(commentId - 1) % replyContents.size]

                    comments.add(
                        CommentDto(
                            id = "comment_$commentId",
                            content = replyContent,
                            authorId = replyAuthor.first,
                            authorName = replyAuthor.second,
                            authorPicture = replyAuthor.third,
                            nodeId = nodeId,
                            parentId = parentCommentId,
                            mentions = emptyList(),
                            replies = emptyList(),
                            createdAt = "2026-01-${((commentId % 28) + 1).toString().padStart(2, '0')}T10:30:00Z",
                            updatedAt = "2026-01-${((commentId % 28) + 1).toString().padStart(2, '0')}T10:30:00Z"
                        )
                    )
                    commentId++
                }
            }
        }
    }
}
