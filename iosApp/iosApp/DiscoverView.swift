import SwiftUI
import Shared
import KMPObservableViewModelSwiftUI

struct DiscoverView: View {
    @StateViewModel var viewModel = KoinHelper().getDiscoverViewModel()

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 24) {
                if isLoading && popularNodes.isEmpty && searchResults.isEmpty {
                    HStack {
                        Spacer()
                        ProgressView()
                        Spacer()
                    }
                    .padding(.top, 40)
                } else if let errorMessage = viewModel.error {
                    errorView(errorMessage)
                } else if !searchQuery.isEmpty {
                    searchResultsSection
                } else {
                    popularTagsSection
                    popularNodesSection
                }
            }
            .padding(.horizontal, 16)
            .padding(.top, 8)
        }
        .refreshable {
            viewModel.loadPopularTags()
            viewModel.loadPopularNodes()
        }
        .navigationTitle("ディスカバー")
        .navigationBarTitleDisplayMode(.inline)
        .searchable(text: searchQueryBinding, prompt: "キーワードで検索...")
        .onSubmit(of: .search) {
            viewModel.search(query: searchQuery)
        }
        .onChange(of: searchQuery) { _, newValue in
            if newValue.isEmpty {
                viewModel.search(query: "")
            }
        }
        .onAppear {
            viewModel.loadPopularTags()
            viewModel.loadPopularNodes()
        }
    }

    // MARK: - Computed Properties

    private var searchQuery: String {
        viewModel.searchQuery
    }

    private var searchQueryBinding: Binding<String> {
        Binding(
            get: { viewModel.searchQuery },
            set: { viewModel.search(query: $0) }
        )
    }

    private var searchResults: [Node] {
        viewModel.searchResults as? [Node] ?? []
    }

    private var popularTags: [Tag] {
        viewModel.popularTags as? [Tag] ?? []
    }

    private var popularNodes: [Node] {
        viewModel.popularNodes as? [Node] ?? []
    }

    private var isLoading: Bool {
        viewModel.isLoading as? Bool ?? false
    }

    // MARK: - Popular Tags Section

    private var popularTagsSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("人気のタグ")
                .font(.headline)

            if popularTags.isEmpty {
                Text("タグがありません")
                    .font(.caption)
                    .foregroundColor(.secondary)
            } else {
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        ForEach(popularTags, id: \.id) { tag in
                            Button(action: {
                                viewModel.selectTag(tag: tag)
                            }) {
                                HStack(spacing: 4) {
                                    Text("#\(tag.name)")
                                        .font(.subheadline)
                                    if tag.usageCount > 0 {
                                        Text("\(tag.usageCount)")
                                            .font(.caption2)
                                            .foregroundColor(.secondary)
                                    }
                                }
                                .padding(.horizontal, 12)
                                .padding(.vertical, 8)
                                .background(Color.blue.opacity(0.1))
                                .foregroundColor(.blue)
                                .cornerRadius(16)
                            }
                            .buttonStyle(.plain)
                        }
                    }
                }
            }
        }
    }

    // MARK: - Popular Nodes Section

    private var popularNodesSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("人気の投稿")
                .font(.headline)

            if popularNodes.isEmpty {
                Text("投稿がありません")
                    .font(.caption)
                    .foregroundColor(.secondary)
                    .padding(.vertical, 8)
            } else {
                LazyVStack(spacing: 12) {
                    ForEach(popularNodes, id: \.id) { node in
                        NavigationLink(destination: DetailView(nodeId: node.id)) {
                            NodeCardView(node: node)
                        }
                        .buttonStyle(.plain)
                    }
                }
            }
        }
    }

    // MARK: - Search Results Section

    private var searchResultsSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("検索結果")
                .font(.headline)

            if isLoading {
                HStack {
                    Spacer()
                    ProgressView()
                    Spacer()
                }
                .padding(.vertical, 16)
            } else if searchResults.isEmpty {
                VStack(spacing: 12) {
                    Image(systemName: "magnifyingglass")
                        .font(.system(size: 40))
                        .foregroundColor(.secondary.opacity(0.5))
                    Text("「\(searchQuery)」に一致する投稿がありません")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                        .multilineTextAlignment(.center)
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical, 32)
            } else {
                LazyVStack(spacing: 12) {
                    ForEach(searchResults, id: \.id) { node in
                        NavigationLink(destination: DetailView(nodeId: node.id)) {
                            NodeCardView(node: node)
                        }
                        .buttonStyle(.plain)
                    }
                }
            }
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
                viewModel.loadPopularTags()
                viewModel.loadPopularNodes()
            }
            .buttonStyle(.bordered)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 32)
    }
}

// MARK: - Preview

#Preview("DiscoverView") {
    NavigationStack {
        DiscoverView()
    }
}
