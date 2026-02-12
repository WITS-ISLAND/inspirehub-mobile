import KMPObservableViewModelSwiftUI
import Shared
import SwiftUI

// MARK: - Tab / Sort UI Enums

enum HomeTabUI: String, CaseIterable {
    case all = "すべて"
    case issue = "課題"
    case idea = "アイデア"
    case mine = "自分"

    var kotlinTab: HomeTab {
        switch self {
        case .all: return .all
        case .issue: return .issues
        case .idea: return .ideas
        case .mine: return .mine
        }
    }

    init(from kotlinTab: HomeTab) {
        switch kotlinTab {
        case .all: self = .all
        case .issues: self = .issue
        case .ideas: self = .idea
        case .mine: self = .mine
        default: self = .all
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

    private var isLoading: Bool {
        viewModel.isLoading as? Bool == true
    }

    private var errorMessage: String? {
        viewModel.error as? String
    }

    var body: some View {
        VStack(spacing: 0) {
            tabBar

            ZStack {
                if isLoading && nodes.isEmpty {
                    ProgressView()
                        .frame(maxHeight: .infinity)
                } else if let error = errorMessage, nodes.isEmpty {
                    errorView(error)
                        .frame(maxHeight: .infinity)
                } else {
                    nodeList
                }
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
            viewModel.onAppear()
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
                                .fill(isCurrentTab(tab) ? Color.appPrimary : Color.clear)
                                .frame(height: 2)
                        }
                        .frame(minHeight: 44)
                        .contentShape(Rectangle())
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
                        NodeCardView(node: node)
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

            HStack(spacing: 4) {
                Label(node.authorName, systemImage: "person")
                    .font(.caption2)
                    .foregroundColor(.secondary)
                    .lineLimit(1)
                Spacer()
            }

            HStack(spacing: 10) {
                inlineReaction(emoji: "👍", count: node.reactions.like.count, isReacted: node.reactions.like.isReacted)
                inlineReaction(
                    emoji: "🔥", count: node.reactions.interested.count, isReacted: node.reactions.interested.isReacted)
                inlineReaction(
                    emoji: "💪", count: node.reactions.wantToTry.count, isReacted: node.reactions.wantToTry.isReacted)
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
        HStack(spacing: 6) {
            Image(systemName: NodeTypeStyle.icon(for: parentNode.type))
                .font(.caption2)
                .foregroundColor(NodeTypeStyle.color(for: parentNode.type))
            Text(NodeTypeStyle.label(for: parentNode.type))
                .font(.caption2)
                .foregroundColor(NodeTypeStyle.color(for: parentNode.type))
            Text("›")
                .font(.caption2)
                .foregroundColor(.secondary)
            Text(parentNode.title)
                .font(.caption2)
                .fontWeight(.medium)
                .foregroundColor(.primary)
                .lineLimit(1)
        }
        .padding(.horizontal, 10)
        .padding(.vertical, 6)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color(.tertiarySystemBackground))
        .cornerRadius(8)
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

    private var nodeTypeBadge: some View {
        HStack(spacing: 4) {
            Image(systemName: NodeTypeStyle.icon(for: node.type))
                .font(.caption2)
            Text(NodeTypeStyle.label(for: node.type))
                .font(.caption2)
                .fontWeight(.medium)
        }
        .padding(.horizontal, 8)
        .padding(.vertical, 4)
        .background(NodeTypeStyle.backgroundColor(for: node.type))
        .foregroundColor(NodeTypeStyle.color(for: node.type))
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

// NOTE: HomeView全体のPreviewはKoinHelper依存のため現状では動作しません。
// 個別コンポーネント（NodeCardView）のPreviewは動作します。
// TODO: Preview用のMock ViewModelを作成して画面全体のPreviewを有効化

// #Preview("HomeView") {
//     NavigationStack {
//         HomeView()
//     }
// }

#Preview("NodeCardView") {
    NodeCardView(node: PreviewData.sampleNode)
        .padding()
}

#Preview("NodeCardView - Derived") {
    NodeCardView(node: PreviewData.sampleDerivedNode)
        .padding()
}
