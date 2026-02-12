import KMPObservableViewModelSwiftUI

import Shared

import SwiftUI

// MARK: - Preview

struct MapView: View {
    @StateViewModel var viewModel = KoinHelper().getMapViewModel()

    private var isLoading: Bool {
        viewModel.isLoading as? Bool == true
    }

    private var errorMessage: String? {
        viewModel.error as? String
    }

    var body: some View {
        ZStack {
            if isLoading && nodes.isEmpty {
                ProgressView("読み込み中...")
            } else if let error = errorMessage, nodes.isEmpty {
                VStack(spacing: 16) {
                    Image(systemName: "exclamationmark.triangle")
                        .font(.system(size: 40))
                        .foregroundColor(.appSecondary)
                    Text(error)
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                        .multilineTextAlignment(.center)
                    Button("再読み込み") {
                        viewModel.loadNodes()
                    }
                }
                .padding()
            } else if nodes.isEmpty {
                VStack(spacing: 16) {
                    Image(systemName: "map")
                        .font(.system(size: 60))
                        .foregroundColor(.secondary.opacity(0.5))
                    Text("ノードがありません")
                        .font(.headline)
                        .foregroundColor(.secondary)
                }
            } else {
                nodeList
            }
        }
        .navigationTitle("マップ")
        .navigationBarTitleDisplayMode(.inline)
        .onAppear {
            viewModel.loadNodes()
        }
    }

    private var nodes: [Node] {
        viewModel.nodes as? [Node] ?? []
    }

    private var nodeList: some View {
        ScrollView {
            LazyVStack(alignment: .leading, spacing: 0) {
                ForEach(nodes, id: \.id) { node in
                    let isChild = node.parentNode != nil
                    NavigationLink(destination: DetailView(nodeId: node.id)) {
                        nodeRow(node: node, indented: isChild)
                    }
                    .buttonStyle(.plain)
                }
            }
        }
        .refreshable {
            viewModel.loadNodes()
        }
    }

    private func nodeRow(node: Node, indented: Bool) -> some View {
        VStack(spacing: 0) {
            HStack(spacing: 12) {
                if indented {
                    Rectangle()
                        .fill(Color.clear)
                        .frame(width: 24)
                }

                Image(systemName: NodeTypeStyle.icon(for: node.type))
                    .foregroundColor(NodeTypeStyle.color(for: node.type))
                    .font(.title3)

                VStack(alignment: .leading, spacing: 4) {
                    Text(node.title)
                        .font(.body)
                        .fontWeight(.medium)
                        .lineLimit(2)

                    Text(node.content)
                        .font(.caption)
                        .foregroundColor(.secondary)
                        .lineLimit(1)
                }

                Spacer()

                Image(systemName: "chevron.right")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)

            Divider()
                .padding(.leading, indented ? 52 : 16)
        }
        .background(Color(.systemBackground))
    }
}

// NOTE: MapView全体のPreviewはKoinHelper依存のため現状では動作しません。
// TODO: Preview用のMock ViewModelを作成して画面全体のPreviewを有効化

// #Preview("MapView") {
//     NavigationStack {
//         MapView()
//     }
// }
