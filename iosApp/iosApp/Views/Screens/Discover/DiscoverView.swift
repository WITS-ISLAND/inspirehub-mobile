import KMPObservableViewModelSwiftUI

import Shared

import SwiftUI

// MARK: - Preview

struct DiscoverView: View {
    @StateViewModel var viewModel = KoinHelper().getDiscoverViewModel()

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 24) {
                if isLoading && popularNodes.isEmpty && searchResults.isEmpty && tagNodes.isEmpty {
                    HStack {
                        Spacer()
                        ProgressView()
                        Spacer()
                    }
                    .padding(.top, 40)
                } else if let errorMessage = viewModel.error {
                    errorView(errorMessage)
                } else if !tagSuggestions.isEmpty {
                    tagSuggestionsSection
                } else if !searchQuery.isEmpty && !searchQuery.hasPrefix("#") {
                    searchResultsSection
                } else if searchQuery.hasPrefix("#") {
                    tagSearchHintSection
                } else if selectedTag != nil {
                    popularTagsSection
                    tagNodesSection
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
        .searchable(text: searchQueryBinding, prompt: "キーワード or #タグ で検索")
        .onSubmit(of: .search) {
            viewModel.submitSearch()
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

    private var selectedTag: Tag? {
        viewModel.selectedTag
    }

    private var tagNodes: [Node] {
        viewModel.tagNodes as? [Node] ?? []
    }

    private var tagSuggestions: [Tag] {
        viewModel.tagSuggestions as? [Tag] ?? []
    }

    private var isLoading: Bool {
        viewModel.isLoading as? Bool ?? false
    }

    // MARK: - Tag Suggestions Section

    private var tagSuggestionsSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("タグ候補")
                .font(.headline)

            ForEach(tagSuggestions, id: \.id) { tag in
                Button(action: {
                    viewModel.selectTagSuggestion(tag: tag)
                }) {
                    HStack(spacing: 8) {
                        Image(systemName: "tag")
                            .foregroundColor(.appPrimary)
                        Text("#\(tag.name)")
                            .font(.body)
                            .foregroundColor(.primary)
                        Spacer()
                        if tag.usageCount > 0 {
                            Text("\(tag.usageCount)件")
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                        Image(systemName: "chevron.right")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                    .padding(.horizontal, 12)
                    .padding(.vertical, 10)
                    .background(Color(.secondarySystemBackground))
                    .cornerRadius(8)
                }
                .buttonStyle(.plain)
            }
        }
    }

    // MARK: - Tag Search Hint

    private var tagSearchHintSection: some View {
        VStack(spacing: 12) {
            Image(systemName: "tag")
                .font(.system(size: 40))
                .foregroundColor(.secondary.opacity(0.5))
            Text("タグ名を入力して検索")
                .font(.subheadline)
                .foregroundColor(.secondary)
            Text("例: #AI、#モバイル")
                .font(.caption)
                .foregroundColor(.secondary.opacity(0.7))
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 32)
    }

    // MARK: - Popular Tags Section

    private var popularTagsSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text("人気のタグ")
                    .font(.headline)

                if selectedTag != nil {
                    Spacer()
                    Button("クリア") {
                        viewModel.clearTagFilter()
                    }
                    .font(.subheadline)
                    .foregroundColor(.appPrimary)
                }
            }

            if popularTags.isEmpty {
                Text("タグがありません")
                    .font(.caption)
                    .foregroundColor(.secondary)
            } else {
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        ForEach(popularTags, id: \.id) { tag in
                            let isSelected = selectedTag?.id == tag.id
                            Button(action: {
                                viewModel.selectTag(tag: tag)
                            }) {
                                HStack(spacing: 4) {
                                    Text("#\(tag.name)")
                                        .font(.subheadline)
                                    if tag.usageCount > 0 {
                                        Text("\(tag.usageCount)")
                                            .font(.caption2)
                                            .foregroundColor(
                                                isSelected ? .white.opacity(0.8) : .secondary)
                                    }
                                }
                                .padding(.horizontal, 12)
                                .padding(.vertical, 8)
                                .background(isSelected ? Color.appPrimary : Color.appPrimary.opacity(0.1))
                                .foregroundColor(isSelected ? .white : .appPrimary)
                                .cornerRadius(16)
                            }
                            .buttonStyle(.plain)
                        }
                    }
                }
            }
        }
    }

    // MARK: - Tag Nodes Section

    private var tagNodesSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            if let tag = selectedTag {
                Text("#\(tag.name) の投稿")
                    .font(.headline)
            }

            if isLoading {
                HStack {
                    Spacer()
                    ProgressView()
                    Spacer()
                }
                .padding(.vertical, 16)
            } else if tagNodes.isEmpty {
                VStack(spacing: 12) {
                    Image(systemName: "tag")
                        .font(.system(size: 40))
                        .foregroundColor(.secondary.opacity(0.5))
                    Text("このタグの投稿はまだありません")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                        .multilineTextAlignment(.center)
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical, 32)
            } else {
                LazyVStack(spacing: 12) {
                    ForEach(tagNodes, id: \.id) { node in
                        NavigationLink(destination: DetailView(nodeId: node.id)) {
                            NodeCardView(node: node)
                        }
                        .buttonStyle(.plain)
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

// NOTE: DiscoverView全体のPreviewはKoinHelper依存のため現状では動作しません。
// TODO: Preview用のMock ViewModelを作成して画面全体のPreviewを有効化

// #Preview("DiscoverView") {
//     NavigationStack {
//         DiscoverView()
//     }
// }
