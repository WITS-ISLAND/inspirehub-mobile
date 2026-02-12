import Foundation
import Shared

/// SwiftUI Preview用のサンプルデータ
enum PreviewData {
    // MARK: - Node Samples

    static var sampleNode: Node {
        Node(
            id: "preview-1",
            type: .idea,
            title: "サンプルアイデア",
            content: "これはプレビュー用のサンプルノードです。実際のデータではありません。",
            authorId: "user-1",
            authorName: "テストユーザー",
            authorPicture: nil,
            parentNode: nil,
            tagIds: ["tag-1", "tag-2"],
            reactions: Reactions(
                like: ReactionSummary(count: 5, isReacted: true),
                interested: ReactionSummary(count: 3, isReacted: false),
                wantToTry: ReactionSummary(count: 1, isReacted: false)
            ),
            commentCount: 3,
            createdAt: "2025-01-15T10:30:00Z",
            updatedAt: nil
        )
    }

    static var sampleIssueNode: Node {
        Node(
            id: "preview-2",
            type: .issue,
            title: "サンプル課題",
            content: "リモートワークで雑談の機会が減っている。チームの一体感が薄れている。",
            authorId: "user-2",
            authorName: "課題提起者",
            authorPicture: nil,
            parentNode: nil,
            tagIds: ["tag-3"],
            reactions: Reactions(
                like: ReactionSummary(count: 12, isReacted: false),
                interested: ReactionSummary(count: 8, isReacted: true),
                wantToTry: ReactionSummary(count: 2, isReacted: false)
            ),
            commentCount: 7,
            createdAt: "2025-01-15T09:30:00Z",
            updatedAt: nil
        )
    }

    static var sampleDerivedNode: Node {
        Node(
            id: "preview-3",
            type: .idea,
            title: "雑談チャンネル自動生成ツール",
            content: "曜日ごとにランダムでペアを組んで雑談チャンネルを自動生成するSlackボットを作る。",
            authorId: "user-1",
            authorName: "テストユーザー",
            authorPicture: nil,
            parentNode: ParentNode(
                id: "preview-2",
                type: .issue,
                title: "サンプル課題",
                content: "これはサンプルの課題内容です。"
            ),
            tagIds: ["tag-1"],
            reactions: Reactions(
                like: ReactionSummary(count: 8, isReacted: false),
                interested: ReactionSummary(count: 5, isReacted: true),
                wantToTry: ReactionSummary(count: 3, isReacted: false)
            ),
            commentCount: 5,
            createdAt: "2025-01-15T11:00:00Z",
            updatedAt: nil
        )
    }

    static var sampleNodes: [Node] {
        [sampleNode, sampleIssueNode, sampleDerivedNode]
    }

    // MARK: - Comment Samples

    static var sampleComment: Comment {
        Comment(
            id: "comment-1",
            nodeId: "preview-1",
            parentId: nil,
            authorId: "user-1",
            authorName: "コメントユーザー",
            authorPicture: nil,
            content: "これは素晴らしいアイデアですね！",
            mentions: [],
            replies: [],
            createdAt: "2025-01-15T11:00:00Z"
        )
    }

    static var sampleCommentWithReplies: Comment {
        Comment(
            id: "comment-2",
            nodeId: "preview-1",
            parentId: nil,
            authorId: "user-2",
            authorName: "返信者",
            authorPicture: nil,
            content: "面白そうですね。どう実装しますか？",
            mentions: [],
            replies: [
                Comment(
                    id: "comment-3",
                    nodeId: "preview-1",
                    parentId: "comment-2",
                    authorId: "user-1",
                    authorName: "テストユーザー",
                    authorPicture: nil,
                    content: "Reactで実装してみようと思います。",
                    mentions: [],
                    replies: [],
                    createdAt: "2025-01-15T11:05:00Z"
                )
            ],
            createdAt: "2025-01-15T11:02:00Z"
        )
    }

    static var sampleComments: [Comment] {
        [sampleComment, sampleCommentWithReplies]
    }

    // MARK: - User Samples

    static var sampleUser: User {
        User(
            id: "user-1",
            handle: "テストユーザー",
            email: "test@example.com",
            picture: nil,
            roleTag: "エンジニア"
        )
    }

    // MARK: - Tag Samples

    static var sampleTag: Tag {
        Tag(
            id: "tag-1",
            name: "モバイル",
            usageCount: 42,
            createdAt: "2025-01-15T10:00:00Z"
        )
    }

    static var sampleTags: [Tag] {
        [
            sampleTag,
            Tag(id: "tag-2", name: "AI", usageCount: 38, createdAt: "2025-01-15T09:00:00Z"),
            Tag(id: "tag-3", name: "チーム開発", usageCount: 25, createdAt: "2025-01-15T08:00:00Z")
        ]
    }
}
