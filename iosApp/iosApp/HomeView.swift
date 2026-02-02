import SwiftUI
import Shared

struct HomeView: View {
    @StateObject var viewModel = HomeViewModelWrapper()
    var onNodeTap: ((Node) -> Void)?

    var body: some View {
        NavigationView {
            VStack(spacing: 0) {
                // Upper tab bar
                tabBar

                // Content
                if viewModel.isLoading && viewModel.nodes.isEmpty {
                    Spacer()
                    ProgressView()
                    Spacer()
                } else if let error = viewModel.error, viewModel.nodes.isEmpty {
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
                viewModel.loadNodes()
            }
        }
    }

    // MARK: - Tab Bar

    private var tabBar: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 0) {
                ForEach(HomeTabUI.allCases, id: \.self) { tab in
                    Button(action: {
                        viewModel.setTab(tab)
                    }) {
                        VStack(spacing: 6) {
                            Text(tab.rawValue)
                                .font(.subheadline)
                                .fontWeight(viewModel.currentTab == tab ? .bold : .regular)
                                .foregroundColor(viewModel.currentTab == tab ? .primary : .secondary)

                            Rectangle()
                                .fill(viewModel.currentTab == tab ? Color.blue : Color.clear)
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

    // MARK: - Sort Menu

    private var sortMenu: some View {
        Menu {
            ForEach(SortOrderUI.allCases, id: \.self) { order in
                Button(action: {
                    viewModel.setSortOrder(order)
                }) {
                    HStack {
                        Text(order.rawValue)
                        if viewModel.sortOrder == order {
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

    // MARK: - Node List

    private var nodeList: some View {
        ScrollView {
            LazyVStack(spacing: 12) {
                ForEach(viewModel.nodes, id: \.id) { node in
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
                viewModel.loadNodes()
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
