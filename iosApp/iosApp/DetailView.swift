import SwiftUI
import Shared
import KMPObservableViewModelSwiftUI

struct DetailView: View {
    let nodeId: String
    @StateViewModel var viewModel = KoinHelper().getDetailViewModel()
    @State private var showDerivedPost = false
    @Environment(\.isAuthenticated) private var isAuthenticated
    @Environment(\.loginRequired) private var loginRequired

    var body: some View {
        Group {
            if let node = viewModel.selectedNode {
                nodeDetailContent(node: node)
            } else if let error = viewModel.error {
                VStack(spacing: 16) {
                    Image(systemName: "exclamationmark.triangle")
                        .font(.system(size: 40))
                        .foregroundColor(.orange)
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
        .navigationTitle("詳細")
        .navigationBarTitleDisplayMode(.inline)
        .onAppear {
            viewModel.loadDetail(nodeId: nodeId)
        }
    }

    private func nodeDetailContent(node: Node) -> some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                headerSection(node: node)
                bodySection(node: node)

                if !node.tagIds.isEmpty {
                    tagChipsSection(node: node)
                }

                metaSection(node: node)

                if let parentNode = node.parentNode {
                    parentSection(parentNode: parentNode)
                }

                reactionBar(node: node)
                deriveButton(node: node)
                childNodesSection
                commentsSection
            }
            .padding(16)
        }
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
                        .background(Color.blue.opacity(0.1))
                        .foregroundColor(.blue)
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
                Image(systemName: "person")
                    .font(.caption)
                    .foregroundColor(.secondary)
                Text("\(node.authorName)：\(node.authorId)")
                    .font(.caption)
                    .foregroundColor(.secondary)
                    .lineLimit(1)
                    .truncationMode(.middle)
            }
            Divider()
        }
    }

    // MARK: - Parent Node

    private func parentSection(parentNode: ParentNode) -> some View {
        NavigationLink(destination: DetailView(nodeId: parentNode.id)) {
            HStack(spacing: 10) {
                Image(systemName: NodeTypeStyle.icon(for: parentNode.type))
                    .font(.title3)
                    .foregroundColor(NodeTypeStyle.color(for: parentNode.type))
                VStack(alignment: .leading, spacing: 4) {
                    HStack(spacing: 4) {
                        Text("派生元")
                            .font(.caption2)
                            .foregroundColor(.secondary)
                        Text(NodeTypeStyle.label(for: parentNode.type))
                            .font(.caption2)
                            .fontWeight(.medium)
                            .foregroundColor(NodeTypeStyle.color(for: parentNode.type))
                    }
                    Text(parentNode.title)
                        .font(.subheadline)
                        .fontWeight(.medium)
                        .foregroundColor(.primary)
                        .lineLimit(2)
                    if let content = parentNode.content, !content.isEmpty {
                        Text(content)
                            .font(.caption)
                            .foregroundStyle(.secondary)
                            .lineLimit(3)
                    }
                }
                Spacer()
                Image(systemName: "chevron.right")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
            .padding(12)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(NodeTypeStyle.color(for: parentNode.type).opacity(0.05))
            .cornerRadius(8)
        }
        .buttonStyle(.plain)
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
                guard isAuthenticated else { loginRequired(); return }
                viewModel.toggleReaction(type: .like)
            }

            reactionButton(
                emoji: "🔥",
                label: "気になる",
                count: node.reactions.interested.count,
                isReacted: node.reactions.interested.isReacted
            ) {
                guard isAuthenticated else { loginRequired(); return }
                viewModel.toggleReaction(type: .interested)
            }

            reactionButton(
                emoji: "💪",
                label: "やってみたい",
                count: node.reactions.wantToTry.count,
                isReacted: node.reactions.wantToTry.isReacted
            ) {
                guard isAuthenticated else { loginRequired(); return }
                viewModel.toggleReaction(type: .wantToTry)
            }
        }
        .padding(.vertical, 4)
    }

    private func reactionButton(emoji: String, label: String, count: Int32, isReacted: Bool, action: @escaping () -> Void) -> some View {
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
            guard isAuthenticated else { loginRequired(); return }
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
            .background(Color.orange)
            .cornerRadius(10)
        }
        .sheet(isPresented: $showDerivedPost) {
            DerivedPostView(parentNode: node)
        }
    }

    // MARK: - Child Nodes

    private var childNodesSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            if !viewModel.childNodes.isEmpty {
                Text("派生ノード")
                    .font(.headline)

                ForEach(viewModel.childNodes, id: \.id) { child in
                    NavigationLink(destination: DetailView(nodeId: child.id)) {
                        HStack(spacing: 8) {
                            Image(systemName: "arrow.turn.down.right")
                                .foregroundColor(.blue)
                                .font(.caption)
                            Text(child.title)
                                .font(.subheadline)
                                .lineLimit(1)
                            Spacer()
                            Image(systemName: "chevron.right")
                                .font(.caption2)
                                .foregroundColor(.secondary)
                        }
                        .padding(10)
                        .background(Color(.secondarySystemBackground))
                        .cornerRadius(8)
                    }
                    .buttonStyle(.plain)
                }
            }
        }
    }

    // MARK: - Comments

    private var commentsSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("コメント")
                .font(.headline)

            if isAuthenticated {
                // Comment input
                HStack(spacing: 8) {
                    TextField("コメントを入力...", text: Binding(
                        get: { viewModel.commentText },
                        set: { viewModel.updateCommentText(text: $0) }
                    ))
                        .textFieldStyle(.roundedBorder)

                    Button(action: {
                        viewModel.submitComment()
                    }) {
                        Image(systemName: "paperplane.fill")
                            .foregroundColor(.blue)
                            .frame(width: 44, height: 44)
                            .contentShape(Rectangle())
                    }
                    .disabled(viewModel.commentText.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty || viewModel.isCommentSubmitting)
                    .accessibilityLabel("コメントを送信")
                }
            } else {
                Button(action: loginRequired) {
                    HStack {
                        Image(systemName: "lock.fill")
                            .font(.caption)
                        Text("ログインしてコメントする")
                            .font(.subheadline)
                    }
                    .foregroundColor(.blue)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 10)
                    .background(Color.blue.opacity(0.05))
                    .cornerRadius(8)
                }
            }

            if viewModel.comments.isEmpty {
                Text("まだコメントはありません")
                    .font(.caption)
                    .foregroundColor(.secondary)
                    .padding(.vertical, 8)
            } else {
                ForEach(viewModel.comments, id: \.id) { comment in
                    commentRow(comment: comment)
                }
            }
        }
    }

    private func commentRow(comment: Comment) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            HStack {
                Image(systemName: "person.circle")
                    .foregroundColor(.secondary)
                Text(comment.authorId)
                    .font(.caption)
                    .fontWeight(.semibold)
                Spacer()
            }
            Text(comment.content)
                .font(.subheadline)
        }
        .padding(10)
        .background(Color(.secondarySystemBackground))
        .cornerRadius(8)
    }
}

// MARK: - Preview

#Preview("DetailView") {
    NavigationStack {
        DetailView(nodeId: "preview-1")
    }
}
