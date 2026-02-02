import SwiftUI
import Shared
import KMPObservableViewModelSwiftUI

struct MapView: View {
    @StateViewModel var viewModel = KoinHelper().getMapViewModel()

    var body: some View {
        Group {
            if viewModel.isLoading as? Bool == true && nodes.isEmpty {
                ProgressView("読み込み中...")
            } else if let error = viewModel.error as? String, nodes.isEmpty {
                VStack(spacing: 16) {
                    Image(systemName: "exclamationmark.triangle")
                        .font(.system(size: 40))
                        .foregroundColor(.orange)
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
                    let isChild = node.parentNodeId != nil
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

                Image(systemName: node.type == .issue ? "exclamationmark.circle.fill" : "lightbulb.fill")
                    .foregroundColor(node.type == .issue ? .red : .orange)
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

// MARK: - Preview

#Preview("MapView") {
    NavigationStack {
        MapView()
    }
}
