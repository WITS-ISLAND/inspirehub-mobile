import SwiftUI
import Shared
import KMPObservableViewModelSwiftUI

// MARK: - Tab / Sort UI Enums

enum HomeTabUI: String, CaseIterable {
    case latest = "新着"
    case issue = "課題"
    case idea = "アイデア"
    case mine = "自分"

    var kotlinTab: HomeTab {
        switch self {
        case .latest: return .recent
        case .issue: return .issues
        case .idea: return .ideas
        case .mine: return .mine
        }
    }

    init(from kotlinTab: HomeTab) {
        switch kotlinTab {
        case .recent: self = .latest
        case .issues: self = .issue
        case .ideas: self = .idea
        case .mine: self = .mine
        default: self = .latest
        }
    }
}

enum SortOrderUI: String, CaseIterable {
    case newest = "新しい順"
    case popular = "人気順"

    var kotlinOrder: Shared.SortOrder {
        switch self {
        case .newest: return .recent
        case .popular: return .popular
        }
    }

    init(from kotlinOrder: Shared.SortOrder) {
        switch kotlinOrder {
        case .recent: self = .newest
        case .popular: self = .popular
        default: self = .newest
        }
    }
}

// MARK: - HomeView

struct HomeView: View {
    @StateViewModel var viewModel = KoinHelper().getHomeViewModel()
    var onNodeTap: ((Node) -> Void)?

    var body: some View {
        VStack(spacing: 0) {
            tabBar

            if viewModel.isLoading as? Bool == true && (viewModel.nodes as? [Node] ?? []).isEmpty {
                Spacer()
                ProgressView()
                Spacer()
            } else if let error = viewModel.error as? String, (viewModel.nodes as? [Node] ?? []).isEmpty {
                Spacer()
                errorView(error)
                Spacer()
            } else {
                nodeList
            }
        }
        .navigationTitle("ホーム")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                sortMenu
            }
        }
        .onAppear {
            viewModel.loadNodes(forceRefresh: false)
        }
    }

    // MARK: - Tab Bar

    private var tabBar: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 0) {
                ForEach(HomeTabUI.allCases, id: \.self) { tab in
                    Button(action: {
                        viewModel.setTab(tab: tab.kotlinTab)
                    }) {
                        VStack(spacing: 6) {
                            Text(tab.rawValue)
                                .font(.subheadline)
                                .fontWeight(isCurrentTab(tab) ? .bold : .regular)
                                .foregroundColor(isCurrentTab(tab) ? .primary : .secondary)

                            Rectangle()
                                .fill(isCurrentTab(tab) ? Color.blue : Color.clear)
                                .frame(height: 2)
                        }
                    }
                    .frame(minWidth: 70)
                    .padding(.horizontal, 8)
                }
            }
            .padding(.horizontal, 8)
        }
        .padding(.top, 4)
        .background(Color(.systemBackground))
    }

    private func isCurrentTab(_ tab: HomeTabUI) -> Bool {
        guard let currentTab = viewModel.currentTab as? HomeTab else { return false }
        return currentTab == tab.kotlinTab
    }

    // MARK: - Sort Menu

    private var sortMenu: some View {
        Menu {
            ForEach(SortOrderUI.allCases, id: \.self) { order in
                Button(action: {
                    viewModel.setSortOrder(order: order.kotlinOrder)
                }) {
                    HStack {
                        Text(order.rawValue)
                        if isCurrentSortOrder(order) {
                            Image(systemName: "checkmark")
                        }
                    }
                }
            }
        } label: {
            Image(systemName: "arrow.up.arrow.down")
                .foregroundColor(.primary)
        }
    }

    private func isCurrentSortOrder(_ order: SortOrderUI) -> Bool {
        guard let currentOrder = viewModel.sortOrder as? Shared.SortOrder else { return false }
        return currentOrder == order.kotlinOrder
    }

    // MARK: - Node List

    private var nodes: [Node] {
        viewModel.nodes as? [Node] ?? []
    }

    private var nodeList: some View {
        ScrollView {
            LazyVStack(spacing: 12) {
                ForEach(nodes, id: \.id) { node in
                    NavigationLink(destination: DetailView(nodeId: node.id)) {
                        NodeCardView(node: node, allNodes: nodes)
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(.horizontal, 16)
            .padding(.top, 8)
        }
        .refreshable {
            viewModel.refresh()
        }
    }

    // MARK: - Error View

    private func errorView(_ message: String) -> some View {
        VStack(spacing: 12) {
            Image(systemName: "exclamationmark.triangle")
                .font(.system(size: 40))
                .foregroundColor(.secondary)
            Text(message)
                .font(.subheadline)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
            Button("再読み込み") {
                viewModel.loadNodes(forceRefresh: false)
            }
            .buttonStyle(.bordered)
        }
        .padding()
    }
}

// MARK: - Node Card

struct NodeCardView: View {
    let node: Node
    var allNodes: [Node] = []
    @State private var isParentExpanded: Bool = false

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                nodeTypeBadge
                Spacer()
                Text(formatDate(node.createdAt))
                    .font(.caption2)
                    .foregroundColor(.secondary)
            }

            Text(node.title)
                .font(.headline)
                .foregroundColor(.primary)
                .lineLimit(2)
                .multilineTextAlignment(.leading)

            Text(node.content)
                .font(.subheadline)
                .foregroundColor(.secondary)
                .lineLimit(3)
                .multilineTextAlignment(.leading)

            if !node.tagIds.isEmpty {
                tagChipsRow
            }

            HStack(spacing: 10) {
                inlineReaction(emoji: "👍", count: node.reactions.like.count, isReacted: node.reactions.like.isReacted)
                inlineReaction(emoji: "🔥", count: node.reactions.interested.count, isReacted: node.reactions.interested.isReacted)
                inlineReaction(emoji: "💪", count: node.reactions.wantToTry.count, isReacted: node.reactions.wantToTry.isReacted)
                Label("\(node.commentCount)", systemImage: "bubble.right")
                    .font(.caption2)
                    .foregroundColor(.secondary)
            }

            if let parentNode = node.parentNode {
                parentNodeBadge(parentNode)
            }
        }
        .padding(14)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color(.secondarySystemBackground))
        .cornerRadius(12)
    }

    // MARK: - Parent Node Badge

    private func parentNodeBadge(_ parentNode: ParentNode) -> some View {
        Button {
            withAnimation(.easeInOut(duration: 0.2)) {
                isParentExpanded.toggle()
            }
        } label: {
            VStack(alignment: .leading, spacing: isParentExpanded ? 6 : 0) {
                HStack(spacing: 6) {
                    Image(systemName: parentNodeIcon(parentNode.type))
                        .font(.caption2)
                        .foregroundColor(parentNodeColor(parentNode.type))
                    Text("派生元")
                        .font(.caption2)
                        .foregroundColor(.secondary)
                    Text(parentNodeTypeLabel(parentNode.type))
                        .font(.caption2)
                        .foregroundColor(parentNodeColor(parentNode.type))
                    Text(parentNode.title)
                        .font(.caption2)
                        .fontWeight(.medium)
                        .foregroundColor(.primary)
                        .lineLimit(isParentExpanded ? nil : 1)
                    Spacer()
                    Image(systemName: "chevron.right")
                        .font(.caption2)
                        .foregroundColor(.secondary)
                        .rotationEffect(.degrees(isParentExpanded ? 90 : 0))
                }

                if isParentExpanded {
                    Divider()
                    VStack(alignment: .leading, spacing: 4) {
                        Text(parentNodeTypeLabel(parentNode.type).replacingOccurrences(of: ":", with: ""))
                            .font(.caption)
                            .foregroundColor(parentNodeColor(parentNode.type))
                            .fontWeight(.semibold)
                        Text(parentNode.title)
                            .font(.caption)
                            .fontWeight(.medium)
                            .foregroundColor(.primary)
                            .multilineTextAlignment(.leading)

                        if let fullParent = allNodes.first(where: { $0.id == parentNode.id }),
                           !fullParent.content.isEmpty {
                            Text(fullParent.content)
                                .font(.caption)
                                .foregroundStyle(.secondary)
                                .lineLimit(3)
                                .multilineTextAlignment(.leading)
                        } else if allNodes.isEmpty || allNodes.first(where: { $0.id == parentNode.id }) == nil {
                            Text("タップして詳細を見る")
                                .font(.caption)
                                .foregroundStyle(.tertiary)
                                .italic()
                        }
                    }
                    .padding(.leading, 4)
                }
            }
            .padding(8)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(Color(.tertiarySystemBackground))
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
        case .issue: return "課題:"
        case .idea: return "アイデア:"
        case .project: return "プロジェクト:"
        default: return ""
        }
    }

    private func inlineReaction(emoji: String, count: Int32, isReacted: Bool) -> some View {
        HStack(spacing: 2) {
            Text(emoji)
                .font(.caption2)
            if count > 0 {
                Text("\(count)")
                    .font(.caption2)
                    .foregroundColor(isReacted ? .blue : .secondary)
            }
        }
    }

    private var tagChipsRow: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 6) {
                ForEach(node.tagIds, id: \.self) { tagId in
                    Text("#\(tagId)")
                        .font(.caption2)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 3)
                        .background(Color.blue.opacity(0.08))
                        .foregroundColor(.blue)
                        .cornerRadius(6)
                }
            }
        }
    }

    private var nodeTypeBadge: some View {
        let isIssue = node.type == .issue
        return HStack(spacing: 4) {
            Image(systemName: isIssue ? "exclamationmark.triangle.fill" : "lightbulb.fill")
                .font(.caption2)
            Text(isIssue ? "課題" : "アイデア")
                .font(.caption2)
                .fontWeight(.medium)
        }
        .padding(.horizontal, 8)
        .padding(.vertical, 3)
        .background(isIssue ? Color.orange.opacity(0.15) : Color.yellow.opacity(0.15))
        .foregroundColor(isIssue ? .orange : .yellow)
        .cornerRadius(6)
    }

    private func formatDate(_ dateString: String) -> String {
        let isoFormatter = ISO8601DateFormatter()
        isoFormatter.formatOptions = [.withInternetDateTime, .withFractionalSeconds]
        guard let date = isoFormatter.date(from: dateString) ?? ISO8601DateFormatter().date(from: dateString) else {
            return dateString
        }
        let formatter = RelativeDateTimeFormatter()
        formatter.locale = Locale(identifier: "ja_JP")
        formatter.unitsStyle = .short
        return formatter.localizedString(for: date, relativeTo: Date())
    }
}

// MARK: - Preview

#Preview("HomeView") {
    NavigationStack {
        HomeView()
    }
}

#Preview("NodeCardView") {
    NodeCardView(node: PreviewData.sampleNode)
        .padding()
}

#Preview("NodeCardView - Derived") {
    NavigationStack {
        NodeCardView(
            node: PreviewData.sampleDerivedNode,
            allNodes: [PreviewData.sampleIssueNode, PreviewData.sampleDerivedNode]
        )
        .padding()
    }
}

enum PreviewData {
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
            parentNode: ParentNode(id: "preview-2", type: .issue, title: "サンプル課題"),
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
}
