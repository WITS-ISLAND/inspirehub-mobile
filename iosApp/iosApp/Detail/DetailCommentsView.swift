import Shared

import SwiftUI

// MARK: - DetailCommentsView

struct DetailCommentsView: View {
    /// コメント一覧
    let comments: [Comment]
    /// 現在ログイン中のユーザーID
    let currentUserId: String?
    /// 編集中のコメントID
    let editingCommentId: String?
    /// 編集中のコメントテキスト
    let editCommentText: String
    /// コメント編集開始時のコールバック
    let onStartEditing: (Comment) -> Void
    /// コメント編集キャンセル時のコールバック
    let onCancelEditing: () -> Void
    /// コメントテキスト更新時のコールバック
    let onUpdateEditText: (String) -> Void
    /// コメント保存時のコールバック
    let onSaveEdit: () -> Void
    /// コメント削除リクエスト時のコールバック
    let onRequestDelete: (String) -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text("コメント")
                    .font(.headline)
                if !comments.isEmpty {
                    Text("\(comments.count)")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                }
            }

            if comments.isEmpty {
                emptyCommentsView
            } else {
                ForEach(comments, id: \.id) { comment in
                    commentRow(comment: comment)
                }
            }
        }
    }

    // MARK: - Empty State

    private var emptyCommentsView: some View {
        VStack(spacing: 8) {
            Image(systemName: "bubble.left.and.bubble.right")
                .font(.system(size: 32))
                .foregroundColor(.secondary.opacity(0.5))
            Text("まだコメントはありません")
                .font(.caption)
                .foregroundColor(.secondary)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 16)
    }

    // MARK: - Comment Row

    private func commentRow(comment: Comment) -> some View {
        Group {
            if editingCommentId == comment.id {
                editingCommentRow(comment: comment)
            } else {
                displayCommentRow(comment: comment)
            }
        }
    }

    // MARK: - Display Comment Row

    private func displayCommentRow(comment: Comment) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            HStack(spacing: 4) {
                UserAvatarView(pictureURL: comment.authorPicture, size: 20)
                Text(comment.authorName)
                    .font(.caption)
                    .fontWeight(.semibold)
                Text("・\(relativeTime(from: comment.createdAt))")
                    .font(.caption)
                    .foregroundColor(.secondary)
                Spacer()
            }
            Text(comment.content)
                .font(.subheadline)
        }
        .padding(10)
        .background(Color(.secondarySystemBackground))
        .cornerRadius(8)
        .contextMenu {
            if isCommentOwner(comment) {
                Button {
                    onStartEditing(comment)
                } label: {
                    Label("編集", systemImage: "pencil")
                }
                Button(role: .destructive) {
                    onRequestDelete(comment.id)
                } label: {
                    Label("削除", systemImage: "trash")
                }
            }
        }
    }

    // MARK: - Editing Comment Row

    private func editingCommentRow(comment: Comment) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                UserAvatarView(pictureURL: comment.authorPicture, size: 20)
                Text(comment.authorName)
                    .font(.caption)
                    .fontWeight(.semibold)
                Spacer()
            }
            TextField("コメントを編集...", text: Binding(
                get: { editCommentText },
                set: { onUpdateEditText($0) }
            ))
            .textFieldStyle(.roundedBorder)
            .font(.subheadline)

            HStack(spacing: 12) {
                Spacer()
                Button("キャンセル") {
                    onCancelEditing()
                }
                .font(.caption)
                .foregroundColor(.secondary)

                Button("保存") {
                    onSaveEdit()
                }
                .font(.caption)
                .fontWeight(.semibold)
                .disabled(
                    editCommentText
                        .trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
                )
            }
        }
        .padding(10)
        .background(Color(.secondarySystemBackground))
        .cornerRadius(8)
        .overlay(
            RoundedRectangle(cornerRadius: 8)
                .stroke(Color.appPrimary, lineWidth: 1)
        )
    }

    // MARK: - Helpers

    private func isCommentOwner(_ comment: Comment) -> Bool {
        guard let userId = currentUserId else { return false }
        return comment.authorId == userId
    }

    private func relativeTime(from isoString: String) -> String {
        let formatter = ISO8601DateFormatter()
        formatter.formatOptions = [.withInternetDateTime, .withFractionalSeconds]
        let date = formatter.date(from: isoString)
            ?? ISO8601DateFormatter().date(from: isoString)
        guard let date else { return "" }
        let relativeFormatter = RelativeDateTimeFormatter()
        relativeFormatter.locale = Locale(identifier: "ja_JP")
        relativeFormatter.unitsStyle = .short
        return relativeFormatter.localizedString(for: date, relativeTo: Date())
    }
}

#Preview("DetailCommentsView - With Comments") {
    DetailCommentsView(
        comments: [
            Comment(
                id: "comment-1",
                nodeId: "node-1",
                parentId: nil,
                authorId: "user-1",
                authorName: "テストユーザー",
                authorPicture: nil,
                content: "素晴らしいアイデアですね！",
                mentions: [],
                replies: [],
                createdAt: "2025-01-15T10:30:00Z"
            ),
            Comment(
                id: "comment-2",
                nodeId: "node-1",
                parentId: nil,
                authorId: "user-2",
                authorName: "別のユーザー",
                authorPicture: nil,
                content: "実装してみたいです。",
                mentions: [],
                replies: [],
                createdAt: "2025-01-15T11:00:00Z"
            ),
        ],
        currentUserId: "user-1",
        editingCommentId: nil,
        editCommentText: "",
        onStartEditing: { _ in },
        onCancelEditing: {},
        onUpdateEditText: { _ in },
        onSaveEdit: {},
        onRequestDelete: { _ in }
    )
    .padding(16)
}

#Preview("DetailCommentsView - Empty") {
    DetailCommentsView(
        comments: [],
        currentUserId: nil,
        editingCommentId: nil,
        editCommentText: "",
        onStartEditing: { _ in },
        onCancelEditing: {},
        onUpdateEditText: { _ in },
        onSaveEdit: {},
        onRequestDelete: { _ in }
    )
    .padding(16)
}
