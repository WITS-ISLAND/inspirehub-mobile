import SwiftUI
import Shared
import KMPObservableViewModelSwiftUI

struct MyPageView: View {
    @StateViewModel var viewModel = KoinHelper().getMyPageViewModel()

    var body: some View {
        ScrollView {
            VStack(spacing: 24) {
                profileSection
                myNodesSection
            }
            .padding(.vertical, 16)
        }
        .navigationTitle("マイページ")
        .navigationBarTitleDisplayMode(.inline)
        .onAppear {
            viewModel.loadMyNodes()
        }
    }

    private var profileSection: some View {
        VStack(spacing: 12) {
            Image(systemName: "person.circle.fill")
                .font(.system(size: 72))
                .foregroundColor(.blue)

            if let user = viewModel.currentUser as? User {
                Text(user.handle)
                    .font(.title2)
                    .fontWeight(.bold)

                if let roleTag = user.roleTag {
                    Text(roleTag)
                        .font(.caption)
                        .padding(.horizontal, 12)
                        .padding(.vertical, 4)
                        .background(Color.blue.opacity(0.1))
                        .cornerRadius(8)
                }
            } else {
                Text("ユーザー情報を取得中...")
                    .font(.subheadline)
                    .foregroundColor(.secondary)
            }
        }
        .padding(.top, 16)
    }

    private var myNodes: [Node] {
        viewModel.myNodes as? [Node] ?? []
    }

    private var myNodesSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text("自分の投稿")
                    .font(.headline)
                Text("\(myNodes.count)")
                    .font(.subheadline)
                    .foregroundColor(.secondary)
            }
            .padding(.horizontal, 16)

            if viewModel.isLoading as? Bool == true && myNodes.isEmpty {
                HStack {
                    Spacer()
                    ProgressView()
                    Spacer()
                }
                .padding(.vertical, 32)
            } else if myNodes.isEmpty {
                VStack(spacing: 12) {
                    Image(systemName: "square.and.pencil")
                        .font(.system(size: 40))
                        .foregroundColor(.secondary.opacity(0.5))
                    Text("まだ投稿がありません")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical, 32)
            } else {
                LazyVStack(spacing: 0) {
                    ForEach(myNodes, id: \.id) { node in
                        NavigationLink(destination: DetailView(nodeId: node.id)) {
                            myNodeRow(node: node)
                        }
                        .buttonStyle(.plain)
                    }
                }
            }
        }
    }

    private func myNodeRow(node: Node) -> some View {
        VStack(spacing: 0) {
            HStack(spacing: 12) {
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
                .padding(.leading, 16)
        }
    }
}

// MARK: - Preview

#Preview("MyPageView") {
    NavigationStack {
        MyPageView()
    }
}
