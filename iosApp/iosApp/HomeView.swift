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
        .navigationTitle("InspireHub")
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

            HStack(spacing: 12) {
                Label("\(node.likeCount)", systemImage: node.isLiked ? "hand.thumbsup.fill" : "hand.thumbsup")
                    .font(.caption2)
                    .foregroundColor(node.isLiked ? .blue : .secondary)
                Label("\(node.commentCount)", systemImage: "bubble.right")
                    .font(.caption2)
                    .foregroundColor(.secondary)
            }
        }
        .padding(14)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color(.secondarySystemBackground))
        .cornerRadius(12)
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

    private func formatDate(_ instant: Kotlinx_datetimeInstant) -> String {
        let seconds = instant.epochSeconds
        let date = Date(timeIntervalSince1970: TimeInterval(seconds))
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

enum PreviewData {
    static var sampleNode: Node {
        Node(
            id: "preview-1",
            type: .idea,
            title: "サンプルアイデア",
            content: "これはプレビュー用のサンプルノードです。実際のデータではありません。",
            authorId: "user-1",
            parentNodeId: nil,
            tagIds: [],
            likeCount: 5,
            isLiked: true,
            commentCount: 3,
            createdAt: Kotlinx_datetimeInstant.companion.fromEpochSeconds(epochSeconds: Int64(Date().timeIntervalSince1970), nanosecondAdjustment: 0),
            updatedAt: Kotlinx_datetimeInstant.companion.fromEpochSeconds(epochSeconds: Int64(Date().timeIntervalSince1970), nanosecondAdjustment: 0)
        )
    }

    static var sampleIssueNode: Node {
        Node(
            id: "preview-2",
            type: .issue,
            title: "サンプル課題",
            content: "リモートワークで雑談の機会が減っている。チームの一体感が薄れている。",
            authorId: "user-2",
            parentNodeId: nil,
            tagIds: [],
            likeCount: 12,
            isLiked: false,
            commentCount: 7,
            createdAt: Kotlinx_datetimeInstant.companion.fromEpochSeconds(epochSeconds: Int64(Date().timeIntervalSince1970) - 3600, nanosecondAdjustment: 0),
            updatedAt: Kotlinx_datetimeInstant.companion.fromEpochSeconds(epochSeconds: Int64(Date().timeIntervalSince1970) - 3600, nanosecondAdjustment: 0)
        )
    }
}
