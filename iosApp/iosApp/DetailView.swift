import SwiftUI
import Shared
import KMPObservableViewModelSwiftUI

struct DetailView: View {
    let nodeId: String
    @StateViewModel var viewModel = KoinHelper().getDetailViewModel()
    @State private var showDerivedPost = false

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
        .navigationTitle(viewModel.selectedNode?.title ?? "詳細")
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
            HStack(spacing: 16) {
                Label(node.authorId, systemImage: "person")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
            Divider()
        }
    }

    // MARK: - Parent Node

    private func parentSection(parentNode: ParentNode) -> some View {
        NavigationLink(destination: DetailView(nodeId: parentNode.id)) {
            HStack(spacing: 8) {
                Image(systemName: parentNodeIcon(parentNode.type))
                    .foregroundColor(parentNodeColor(parentNode.type))
                VStack(alignment: .leading, spacing: 2) {
                    HStack(spacing: 4) {
                        Text("派生元")
                            .font(.caption2)
                            .foregroundColor(.secondary)
                        Text(parentNodeTypeLabel(parentNode.type))
                            .font(.caption2)
                            .foregroundColor(parentNodeColor(parentNode.type))
                    }
                    Text(parentNode.title)
                        .font(.subheadline)
                        .foregroundColor(.primary)
                        .lineLimit(2)
                }
                Spacer()
                Image(systemName: "chevron.right")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
            .padding(12)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(Color.blue.opacity(0.05))
            .cornerRadius(8)
        }
        .buttonStyle(.plain)
    }

    private func parentNodeIcon(_ type: NodeType) -> String {
        switch type {
        case .issue: return "exclamationmark.triangle.fill"
        case .idea: return "lightbulb.fill"
        case .project: return "folder.fill"
        default: return "doc.fill"
        }
    }

    private func parentNodeColor(_ type: NodeType) -> Color {
        switch type {
        case .issue: return .orange
        case .idea: return .yellow
        case .project: return .blue
        default: return .secondary
        }
    }

    private func parentNodeTypeLabel(_ type: NodeType) -> String {
        switch type {
        case .issue: return "課題"
        case .idea: return "アイデア"
        case .project: return "プロジェクト"
        default: return ""
        }
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
                viewModel.toggleReaction(type: .like)
            }

            reactionButton(
                emoji: "🔥",
                label: "気になる",
                count: node.reactions.interested.count,
                isReacted: node.reactions.interested.isReacted
            ) {
                viewModel.toggleReaction(type: .interested)
            }

            reactionButton(
                emoji: "💪",
                label: "やってみたい",
                count: node.reactions.wantToTry.count,
                isReacted: node.reactions.wantToTry.isReacted
            ) {
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
                    .font(.system(size: 9))
                    .foregroundColor(isReacted ? .blue : .secondary)
            }
        }
        .buttonStyle(.plain)
    }

    // MARK: - Derive Button

    private func deriveButton(node: Node) -> some View {
        Button(action: {
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
                    set: { viewModel.updateCommentText(text: $0) }
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

// MARK: - Preview

#Preview("DetailView") {
    NavigationStack {
        DetailView(nodeId: "preview-1")
    }
}
