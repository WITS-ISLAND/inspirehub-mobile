import SwiftUI
import Shared

struct DetailView: View {
    let nodeId: String
    @StateObject private var viewModel = DetailViewModelWrapper()

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
                metaSection(node: node)

                if node.parentNodeId != nil {
                    parentSection(parentNodeId: node.parentNodeId!)
                }

                reactionBar(node: node)
                deriveButton
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
                Image(systemName: node.type == .issue ? "exclamationmark.circle.fill" : "lightbulb.fill")
                    .foregroundColor(node.type == .issue ? .red : .orange)
                Text(node.type == .issue ? "課題" : "アイデア")
                    .font(.caption)
                    .fontWeight(.semibold)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 2)
                    .background(node.type == .issue ? Color.red.opacity(0.1) : Color.orange.opacity(0.1))
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

    // MARK: - Meta

    private func metaSection(node: Node) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            Divider()
            HStack(spacing: 16) {
                Label(node.authorId, systemImage: "person")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
            Divider()
        }
    }

    // MARK: - Parent Node

    private func parentSection(parentNodeId: String) -> some View {
        NavigationLink(destination: DetailView(nodeId: parentNodeId)) {
            HStack(spacing: 8) {
                Image(systemName: "arrow.turn.up.left")
                    .foregroundColor(.blue)
                Text("派生元ノードを見る")
                    .font(.subheadline)
                    .foregroundColor(.blue)
            }
            .padding(12)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(Color.blue.opacity(0.05))
            .cornerRadius(8)
        }
    }

    // MARK: - Reactions

    private func reactionBar(node: Node) -> some View {
        HStack(spacing: 16) {
            reactionButton(emoji: "👍", label: "いいね") {
                viewModel.toggleLike()
            }
            reactionButton(emoji: "💡", label: "共感") { }
            reactionButton(emoji: "👀", label: "気になる") { }
            reactionButton(emoji: "🤝", label: "作りたい") { }
        }
        .padding(.vertical, 4)
    }

    private func reactionButton(emoji: String, label: String, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            VStack(spacing: 2) {
                Text(emoji)
                    .font(.title3)
                Text(label)
                    .font(.system(size: 9))
                    .foregroundColor(.secondary)
            }
        }
        .buttonStyle(.plain)
    }

    // MARK: - Derive Button

    private var deriveButton: some View {
        Button(action: {
            // Phase 2: 派生投稿画面遷移
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
                                .foregroundColor(.orange)
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

            // Comment input
            HStack(spacing: 8) {
                TextField("コメントを入力...", text: Binding(
                    get: { viewModel.commentText },
                    set: { viewModel.updateCommentText($0) }
                ))
                    .textFieldStyle(.roundedBorder)

                Button(action: {
                    viewModel.submitComment()
                }) {
                    Image(systemName: "paperplane.fill")
                        .foregroundColor(.blue)
                }
                .disabled(viewModel.commentText.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty || viewModel.isCommentSubmitting)
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
