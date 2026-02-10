import KMPObservableViewModelSwiftUI

import Shared

import SwiftUI

// MARK: - Preview

struct DetailView: View {
    let nodeId: String
    @StateViewModel var viewModel = KoinHelper().getDetailViewModel()
    @State private var showDerivedPost = false
    @State private var showDeleteAlert = false
    @State private var showDeleteCommentAlert = false
    @State private var commentToDelete: String?
    @Environment(\.isAuthenticated) private var isAuthenticated
    @Environment(\.currentUserId) private var currentUserId
    @Environment(\.loginRequired) private var loginRequired
    @Environment(\.fabHiddenBinding) private var fabHiddenBinding
    @Environment(\.dismiss) private var dismiss

    private var isOwner: Bool {
        guard let userId = currentUserId,
              let node = viewModel.selectedNode else { return false }
        return node.authorId == userId
    }

    var body: some View {
        ZStack {
            if viewModel.isDeleted as? Bool == true {
                deletedView
            } else if let node = viewModel.selectedNode {
                if viewModel.isEditing as? Bool == true {
                    editingContent(node: node)
                } else {
                    nodeDetailContent(node: node)
                }
            } else if let error = viewModel.error {
                VStack(spacing: 16) {
                    Image(systemName: "exclamationmark.triangle")
                        .font(.system(size: 40))
                        .foregroundColor(.appSecondary)
                    Text(error)
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                    Button("再読み込み") {
                        viewModel.loadDetail(nodeId: nodeId)
                    }
                    .buttonStyle(.bordered)
                }
            } else {
                ProgressView("読み込み中...")
            }
        }
        .navigationTitle(viewModel.isEditing as? Bool == true ? "編集" : "詳細")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            if isOwner && viewModel.isEditing as? Bool != true && viewModel.isDeleted as? Bool != true {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Menu {
                        Button(action: { viewModel.startEditing() }) {
                            Label("編集", systemImage: "pencil")
                        }
                        Button(role: .destructive, action: { showDeleteAlert = true }) {
                            Label("削除", systemImage: "trash")
                        }
                    } label: {
                        Image(systemName: "ellipsis.circle")
                    }
                }
            }
            if viewModel.isEditing as? Bool == true {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("キャンセル") {
                        viewModel.cancelEditing()
                    }
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("保存") {
                        viewModel.saveEdit()
                    }
                    .fontWeight(.semibold)
                    .disabled(
                        (viewModel.editTitle as? String ?? "").trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
                            || viewModel.isLoading as? Bool == true
                    )
                }
            }
        }
        .alert("投稿を削除", isPresented: $showDeleteAlert) {
            Button("キャンセル", role: .cancel) {}
            Button("削除", role: .destructive) {
                viewModel.deleteNode()
            }
        } message: {
            Text("この投稿を削除しますか？この操作は取り消せません。")
        }
        .alert("コメントを削除", isPresented: $showDeleteCommentAlert) {
            Button("キャンセル", role: .cancel) {
                commentToDelete = nil
            }
            Button("削除", role: .destructive) {
                if let id = commentToDelete {
                    viewModel.deleteComment(commentId: id)
                    commentToDelete = nil
                }
            }
        } message: {
            Text("このコメントを削除しますか？")
        }
        .onAppear {
            fabHiddenBinding.wrappedValue = true
            viewModel.loadDetail(nodeId: nodeId)
        }
        .onDisappear {
            fabHiddenBinding.wrappedValue = false
        }
    }

    // MARK: - Deleted View

    private var deletedView: some View {
        VStack(spacing: 16) {
            Image(systemName: "checkmark.circle.fill")
                .font(.system(size: 48))
                .foregroundColor(.green)
            Text("投稿を削除しました")
                .font(.headline)
            Button("戻る") {
                dismiss()
            }
            .buttonStyle(.bordered)
        }
    }

    // MARK: - Editing Content

    private func editingContent(node: Node) -> some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                // Type badge (read-only)
                HStack(spacing: 8) {
                    Image(systemName: NodeTypeStyle.icon(for: node.type))
                        .foregroundColor(NodeTypeStyle.color(for: node.type))
                    Text(NodeTypeStyle.label(for: node.type))
                        .font(.caption)
                        .fontWeight(.semibold)
                        .foregroundColor(NodeTypeStyle.color(for: node.type))
                        .padding(.horizontal, 8)
                        .padding(.vertical, 2)
                        .background(NodeTypeStyle.backgroundColor(for: node.type))
                        .cornerRadius(4)
                }

                VStack(alignment: .leading, spacing: 8) {
                    Text("タイトル")
                        .font(.subheadline)
                        .fontWeight(.semibold)
                        .foregroundColor(.secondary)
                    TextField("タイトル", text: Binding(
                        get: { viewModel.editTitle as? String ?? "" },
                        set: { viewModel.updateEditTitle(title: $0) }
                    ))
                    .textFieldStyle(.roundedBorder)
                    .font(.body)
                }

                VStack(alignment: .leading, spacing: 8) {
                    Text("内容")
                        .font(.subheadline)
                        .fontWeight(.semibold)
                        .foregroundColor(.secondary)
                    TextEditor(text: Binding(
                        get: { viewModel.editContent as? String ?? "" },
                        set: { viewModel.updateEditContent(content: $0) }
                    ))
                    .frame(minHeight: 200)
                    .overlay(
                        RoundedRectangle(cornerRadius: 8)
                            .stroke(Color(.systemGray4), lineWidth: 1)
                    )
                }

                if let error = viewModel.error {
                    Text(error)
                        .font(.caption)
                        .foregroundColor(.red)
                }

                if viewModel.isLoading as? Bool == true {
                    HStack {
                        Spacer()
                        ProgressView()
                        Spacer()
                    }
                }
            }
            .padding(16)
        }
    }

    // MARK: - Detail Content

    private func nodeDetailContent(node: Node) -> some View {
        VStack(spacing: 0) {
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    headerSection(node: node)
                    bodySection(node: node)

                    if !node.tagIds.isEmpty {
                        tagChipsSection(node: node)
                    }

                    metaSection(node: node)

                    reactionBar(node: node)
                    deriveButton(node: node)

                    derivationTreeSection(node: node)
                    commentsSection
                }
                .padding(16)
            }
            .refreshable {
                viewModel.refreshDetail(nodeId: nodeId)
            }

            commentInputBar
        }
        .toolbar(.hidden, for: .tabBar)
    }

    // MARK: - Header

    private func headerSection(node: Node) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack(spacing: 8) {
                Image(systemName: NodeTypeStyle.icon(for: node.type))
                    .foregroundColor(NodeTypeStyle.color(for: node.type))
                Text(NodeTypeStyle.label(for: node.type))
                    .font(.caption)
                    .fontWeight(.semibold)
                    .foregroundColor(NodeTypeStyle.color(for: node.type))
                    .padding(.horizontal, 8)
                    .padding(.vertical, 2)
                    .background(NodeTypeStyle.backgroundColor(for: node.type))
                    .cornerRadius(4)
            }

            Text(node.title)
                .font(.title2)
                .fontWeight(.bold)
        }
    }

    // MARK: - Body

    private func bodySection(node: Node) -> some View {
        Text(node.content)
            .font(.body)
            .lineSpacing(4)
    }

    // MARK: - Tags

    private func tagChipsSection(node: Node) -> some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                ForEach(node.tagIds, id: \.self) { tagId in
                    Text("#\(tagId)")
                        .font(.caption)
                        .padding(.horizontal, 10)
                        .padding(.vertical, 5)
                        .background(Color.appPrimary.opacity(0.1))
                        .foregroundColor(.appPrimary)
                        .cornerRadius(8)
                }
            }
        }
    }

    // MARK: - Meta

    private func metaSection(node: Node) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            Divider()
            HStack(spacing: 6) {
                UserAvatarView(pictureURL: node.authorPicture, size: 16)
                Text(node.authorName)
                    .font(.caption)
                    .foregroundColor(.secondary)
                    .lineLimit(1)
                    .truncationMode(.middle)
            }
            Divider()
        }
    }

    // MARK: - Derivation Tree

    private func derivationTreeSection(node: Node) -> some View {
        let hasParent = node.parentNode != nil
        let hasChildren = !viewModel.childNodes.isEmpty

        return Group {
            if hasParent || hasChildren {
                VStack(alignment: .leading, spacing: 0) {
                    // Section header
                    HStack(spacing: 6) {
                        Image(systemName: "arrow.triangle.branch")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                        Text("派生ツリー")
                            .font(.headline)
                    }
                    .padding(.bottom, 12)

                    // Tree items
                    VStack(alignment: .leading, spacing: 0) {
                        if let parentNode = node.parentNode {
                            let isLast = !hasChildren
                            derivationTreeItem(
                                label: "派生元",
                                type: parentNode.type,
                                title: parentNode.title,
                                content: parentNode.content,
                                nodeId: parentNode.id,
                                isLast: isLast
                            )
                        }

                        ForEach(Array(viewModel.childNodes.enumerated()), id: \.element.id) { index, child in
                            let isLast = index == viewModel.childNodes.count - 1
                            derivationTreeItem(
                                label: "派生先",
                                type: child.type,
                                title: child.title,
                                content: child.content,
                                nodeId: child.id,
                                isLast: isLast
                            )
                        }
                    }
                }
            }
        }
    }

    private func derivationTreeItem(
        label: String,
        type: NodeType,
        title: String,
        content: String?,
        nodeId: String,
        isLast: Bool
    ) -> some View {
        HStack(alignment: .top, spacing: 0) {
            // Tree connector line
            treeConnector(isLast: isLast)

            // Card
            NavigationLink(destination: DetailView(nodeId: nodeId)) {
                derivationCard(
                    label: label,
                    type: type,
                    title: title,
                    content: content
                )
            }
            .buttonStyle(.plain)
        }
    }

    private func treeConnector(isLast: Bool) -> some View {
        VStack(spacing: 0) {
            // Branch symbol: top vertical line + horizontal connector
            HStack(alignment: .top, spacing: 0) {
                // Vertical line (left side)
                Rectangle()
                    .fill(Color.secondary.opacity(0.3))
                    .frame(width: 2)

                // Horizontal connector
                Rectangle()
                    .fill(Color.secondary.opacity(0.3))
                    .frame(width: 12, height: 2)
                    .padding(.top, 18)
            }
            .frame(width: 14)

            // Continuation line below (only if not last)
            if !isLast {
                Rectangle()
                    .fill(Color.secondary.opacity(0.3))
                    .frame(width: 2)
                    .frame(maxHeight: .infinity)
                    .frame(width: 14, alignment: .leading)
            } else {
                Spacer()
                    .frame(width: 14)
            }
        }
        .padding(.trailing, 8)
    }

    private func derivationCard(
        label: String,
        type: NodeType,
        title: String,
        content: String?
    ) -> some View {
        HStack(spacing: 10) {
            Image(systemName: NodeTypeStyle.icon(for: type))
                .font(.title3)
                .foregroundColor(NodeTypeStyle.color(for: type))

            VStack(alignment: .leading, spacing: 4) {
                HStack(spacing: 4) {
                    Text(label)
                        .font(.caption2)
                        .foregroundColor(.secondary)
                    Text(NodeTypeStyle.label(for: type))
                        .font(.caption2)
                        .fontWeight(.medium)
                        .foregroundColor(NodeTypeStyle.color(for: type))
                }
                Text(title)
                    .font(.subheadline)
                    .fontWeight(.medium)
                    .foregroundColor(.primary)
                    .lineLimit(2)
                if let content, !content.isEmpty {
                    Text(content)
                        .font(.caption)
                        .foregroundStyle(.secondary)
                        .lineLimit(2)
                }
            }

            Spacer()

            Image(systemName: "chevron.right")
                .font(.caption)
                .foregroundColor(.secondary)
        }
        .padding(12)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(NodeTypeStyle.color(for: type).opacity(0.05))
        .cornerRadius(8)
        .padding(.vertical, 4)
    }

    // MARK: - Reactions

    private func reactionBar(node: Node) -> some View {
        HStack(spacing: 16) {
            reactionButton(
                emoji: "👍",
                label: "いいね",
                count: node.reactions.like.count,
                isReacted: node.reactions.like.isReacted
            ) {
                guard isAuthenticated else {
                    loginRequired()
                    return
                }
                viewModel.toggleReaction(type: .like)
            }

            reactionButton(
                emoji: "🔥",
                label: "気になる",
                count: node.reactions.interested.count,
                isReacted: node.reactions.interested.isReacted
            ) {
                guard isAuthenticated else {
                    loginRequired()
                    return
                }
                viewModel.toggleReaction(type: .interested)
            }

            reactionButton(
                emoji: "💪",
                label: "やってみたい",
                count: node.reactions.wantToTry.count,
                isReacted: node.reactions.wantToTry.isReacted
            ) {
                guard isAuthenticated else {
                    loginRequired()
                    return
                }
                viewModel.toggleReaction(type: .wantToTry)
            }
        }
        .padding(.vertical, 4)
    }

    private func reactionButton(
        emoji: String, label: String, count: Int32, isReacted: Bool, action: @escaping () -> Void
    ) -> some View {
        Button(action: action) {
            VStack(spacing: 2) {
                Text(emoji)
                    .font(.title3)
                Text(count > 0 ? "\(label) \(count)" : label)
                    .font(.system(size: 10))
                    .foregroundColor(isReacted ? .blue : .secondary)
            }
            .frame(minWidth: 44, minHeight: 44)
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .accessibilityLabel("\(label) \(count)件\(isReacted ? " リアクション済み" : "")")
    }

    // MARK: - Derive Button

    private func deriveButton(node: Node) -> some View {
        Button(action: {
            guard isAuthenticated else {
                loginRequired()
                return
            }
            showDerivedPost = true
        }) {
            HStack {
                Image(systemName: "plus.bubble")
                Text("派生アイデアを投稿")
            }
            .font(.subheadline)
            .fontWeight(.semibold)
            .foregroundColor(.white)
            .frame(maxWidth: .infinity)
            .padding(12)
            .background(Color.appSecondary)
            .cornerRadius(10)
        }
        .sheet(isPresented: $showDerivedPost) {
            DerivedPostView(parentNode: node)
        }
    }


    // MARK: - Comments

    private var commentsSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text("コメント")
                    .font(.headline)
                if !viewModel.comments.isEmpty {
                    Text("\(viewModel.comments.count)")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                }
            }

            if viewModel.comments.isEmpty {
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
            } else {
                ForEach(viewModel.comments, id: \.id) { comment in
                    commentRow(comment: comment)
                }
            }
        }
    }

    // MARK: - Comment Input Bar

    private var commentInputBar: some View {
        Group {
            if isAuthenticated {
                VStack(spacing: 0) {
                    Divider()
                    HStack(spacing: 8) {
                        TextField(
                            "コメントを入力...",
                            text: Binding(
                                get: { viewModel.commentText },
                                set: { viewModel.updateCommentText(text: $0) }
                            )
                        )
                        .textFieldStyle(.roundedBorder)

                        if viewModel.isCommentSubmitting {
                            ProgressView()
                                .frame(width: 44, height: 44)
                        } else {
                            Button(action: {
                                viewModel.submitComment()
                            }) {
                                Image(systemName: "paperplane.fill")
                                    .foregroundColor(.appPrimary)
                                    .frame(width: 44, height: 44)
                                    .contentShape(Rectangle())
                            }
                            .disabled(
                                viewModel.commentText.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
                            )
                            .accessibilityLabel("コメントを送信")
                        }
                    }
                    .padding(.horizontal, 16)
                    .padding(.vertical, 8)
                }
                .background(Color(.systemBackground))
            } else {
                VStack(spacing: 0) {
                    Divider()
                    Button(action: loginRequired) {
                        HStack {
                            Image(systemName: "lock.fill")
                                .font(.caption)
                            Text("ログインしてコメントする")
                                .font(.subheadline)
                        }
                        .foregroundColor(.appPrimary)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 10)
                    }
                    .padding(.horizontal, 16)
                    .padding(.vertical, 8)
                }
                .background(Color(.systemBackground))
            }
        }
    }

    private var isCommentOwner: (Comment) -> Bool {
        { comment in
            guard let userId = currentUserId else { return false }
            return comment.authorId == userId
        }
    }

    private func commentRow(comment: Comment) -> some View {
        Group {
            if viewModel.editingCommentId as? String == comment.id {
                editingCommentRow(comment: comment)
            } else {
                displayCommentRow(comment: comment)
            }
        }
    }

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
                    viewModel.startEditingComment(comment: comment)
                } label: {
                    Label("編集", systemImage: "pencil")
                }
                Button(role: .destructive) {
                    commentToDelete = comment.id
                    showDeleteCommentAlert = true
                } label: {
                    Label("削除", systemImage: "trash")
                }
            }
        }
    }

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
                get: { viewModel.editCommentText as? String ?? "" },
                set: { viewModel.updateEditCommentText(text: $0) }
            ))
            .textFieldStyle(.roundedBorder)
            .font(.subheadline)

            HStack(spacing: 12) {
                Spacer()
                Button("キャンセル") {
                    viewModel.cancelEditingComment()
                }
                .font(.caption)
                .foregroundColor(.secondary)

                Button("保存") {
                    viewModel.saveCommentEdit()
                }
                .font(.caption)
                .fontWeight(.semibold)
                .disabled(
                    (viewModel.editCommentText as? String ?? "")
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
#Preview("DetailView") {
    NavigationStack {
        DetailView(nodeId: "preview-1")
    }
}
