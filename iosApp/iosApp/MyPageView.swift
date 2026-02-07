import SwiftUI
import Shared
import KMPObservableViewModelSwiftUI

struct MyPageView: View {
    @StateViewModel var viewModel = KoinHelper().getMyPageViewModel()
    @State private var showLogoutAlert = false

    var body: some View {
        ScrollView {
            VStack(spacing: 24) {
                profileSection
                myNodesSection
                reactedNodesSection
                logoutSection
            }
            .padding(.vertical, 16)
        }
        .navigationTitle("マイページ")
        .navigationBarTitleDisplayMode(.inline)
        .onAppear {
            viewModel.refresh()
        }
        .alert("ログアウト", isPresented: $showLogoutAlert) {
            Button("キャンセル", role: .cancel) { }
            Button("ログアウト", role: .destructive) {
                viewModel.logout()
            }
        } message: {
            Text("ログアウトしますか？")
        }
    }

    // MARK: - Profile Section

    private var profileSection: some View {
        VStack(spacing: 12) {
            Image(systemName: "person.circle.fill")
                .font(.system(size: 72))
                .foregroundColor(.blue)

            if let user = viewModel.currentUser as? User {
                if viewModel.isEditingName as? Bool == true {
                    nameEditingView
                } else {
                    nameDisplayView(user: user)
                }

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

            if let error = viewModel.error as? String {
                Text(error)
                    .font(.caption)
                    .foregroundColor(.red)
            }
        }
        .padding(.top, 16)
    }

    private func nameDisplayView(user: User) -> some View {
        HStack(spacing: 8) {
            Text(user.handle)
                .font(.title2)
                .fontWeight(.bold)

            Button(action: {
                viewModel.startEditingName()
            }) {
                Image(systemName: "pencil.circle")
                    .foregroundColor(.blue)
                    .font(.title3)
            }
        }
    }

    private var nameEditingView: some View {
        VStack(spacing: 8) {
            TextField("名前を入力", text: Binding(
                get: { viewModel.editingName as? String ?? "" },
                set: { viewModel.updateEditingName(name: $0) }
            ))
            .textFieldStyle(.roundedBorder)
            .padding(.horizontal, 32)

            HStack(spacing: 12) {
                Button("キャンセル") {
                    viewModel.cancelEditingName()
                }
                .foregroundColor(.secondary)

                Button("保存") {
                    viewModel.updateUserName()
                }
                .fontWeight(.semibold)
                .disabled(viewModel.isUpdatingName as? Bool == true)
            }

            if viewModel.isUpdatingName as? Bool == true {
                ProgressView()
            }
        }
    }

    // MARK: - My Nodes Section

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
                emptyStateView(icon: "square.and.pencil", message: "まだ投稿がありません")
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

    // MARK: - Reacted Nodes Section

    private var reactedNodes: [Node] {
        viewModel.reactedNodes as? [Node] ?? []
    }

    private var reactedNodesSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text("リアクション済み")
                    .font(.headline)
                Text("\(reactedNodes.count)")
                    .font(.subheadline)
                    .foregroundColor(.secondary)
            }
            .padding(.horizontal, 16)

            if reactedNodes.isEmpty {
                emptyStateView(icon: "hand.thumbsup", message: "リアクションした投稿はありません")
            } else {
                LazyVStack(spacing: 0) {
                    ForEach(reactedNodes, id: \.id) { node in
                        NavigationLink(destination: DetailView(nodeId: node.id)) {
                            myNodeRow(node: node)
                        }
                        .buttonStyle(.plain)
                    }
                }
            }
        }
    }

    // MARK: - Logout Section

    private var logoutSection: some View {
        Button(action: {
            showLogoutAlert = true
        }) {
            HStack {
                Image(systemName: "rectangle.portrait.and.arrow.right")
                Text("ログアウト")
            }
            .foregroundColor(.red)
            .frame(maxWidth: .infinity)
            .padding(.vertical, 12)
        }
        .padding(.horizontal, 16)
        .padding(.top, 8)
    }

    // MARK: - Shared Components

    private func emptyStateView(icon: String, message: String) -> some View {
        VStack(spacing: 12) {
            Image(systemName: icon)
                .font(.system(size: 40))
                .foregroundColor(.secondary.opacity(0.5))
            Text(message)
                .font(.subheadline)
                .foregroundColor(.secondary)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 32)
    }

    private func myNodeRow(node: Node) -> some View {
        VStack(spacing: 0) {
            HStack(spacing: 12) {
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
