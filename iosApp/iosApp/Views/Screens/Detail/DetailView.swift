import KMPObservableViewModelSwiftUI

import Shared

import SwiftUI

// MARK: - DetailView

/// ノード詳細画面
///
/// ノードの詳細情報（タイトル、本文、タグ、リアクション、コメント、派生ツリー）を表示し、
/// 編集・削除・派生投稿などの操作を提供する。
struct DetailView: View {
    /// 表示するノードのID
    let nodeId: String
    /// 詳細画面のViewModel
    @StateViewModel var viewModel = KoinHelper().getDetailViewModel()
    /// 削除確認アラート表示フラグ
    @State private var showDeleteAlert = false
    /// コメント削除確認アラート表示フラグ
    @State private var showDeleteCommentAlert = false
    /// 削除対象のコメントID
    @State private var commentToDelete: String?
    @Environment(\.isAuthenticated) private var isAuthenticated
    @Environment(\.currentUserId) private var currentUserId
    @Environment(\.loginRequired) private var loginRequired
    @Environment(\.fabHiddenBinding) private var fabHiddenBinding
    @Environment(\.dismiss) private var dismiss

    private var isOwner: Bool {
        guard let userId = currentUserId,
              let node = viewModel.selectedNode else { return false }
        return node.authorId == userId
    }

    var body: some View {
        ZStack {
            if viewModel.isDeleted == true {
                deletedView
            } else if let node = viewModel.selectedNode {
                if viewModel.isEditing == true {
                    DetailEditView(
                        node: node,
                        editTitle: Binding(
                            get: { viewModel.editTitle },
                            set: { viewModel.updateEditTitle(title: $0) }
                        ),
                        editContent: Binding(
                            get: { viewModel.editContent },
                            set: { viewModel.updateEditContent(content: $0) }
                        ),
                        error: viewModel.error,
                        isLoading: viewModel.isLoading
                    )
                } else {
                    nodeDetailContent(node: node)
                }
            } else if let error = viewModel.error {
                VStack(spacing: 16) {
                    Image(systemName: "exclamationmark.triangle")
                        .font(.system(size: 40))
                        .foregroundColor(.appSecondary)
                    Text(error)
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                    Button("再読み込み") {
                        viewModel.loadDetail(nodeId: nodeId)
                    }
                    .buttonStyle(.bordered)
                }
            } else {
                ProgressView("読み込み中...")
            }
        }
        .navigationTitle(viewModel.isEditing == true ? "編集" : "詳細")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            if isOwner && viewModel.isEditing != true && viewModel.isDeleted != true {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Menu {
                        Button(action: { viewModel.startEditing() }) {
                            Label("編集", systemImage: "pencil")
                        }
                        Button(role: .destructive, action: { showDeleteAlert = true }) {
                            Label("削除", systemImage: "trash")
                        }
                    } label: {
                        Image(systemName: "ellipsis.circle")
                    }
                }
            }
            if viewModel.isEditing == true {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("キャンセル") {
                        viewModel.cancelEditing()
                    }
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("保存") {
                        viewModel.saveEdit()
                    }
                    .fontWeight(.semibold)
                    .disabled(
                        viewModel.editTitle.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
                            || viewModel.isLoading == true
                    )
                }
            }
        }
        .alert("投稿を削除", isPresented: $showDeleteAlert) {
            Button("キャンセル", role: .cancel) {}
            Button("削除", role: .destructive) {
                viewModel.deleteNode()
            }
        } message: {
            Text("この投稿を削除しますか？この操作は取り消せません。")
        }
        .alert("コメントを削除", isPresented: $showDeleteCommentAlert) {
            Button("キャンセル", role: .cancel) {
                commentToDelete = nil
            }
            Button("削除", role: .destructive) {
                if let id = commentToDelete {
                    viewModel.deleteComment(commentId: id)
                    commentToDelete = nil
                }
            }
        } message: {
            Text("このコメントを削除しますか？")
        }
        .onAppear {
            fabHiddenBinding.wrappedValue = true
            viewModel.loadDetail(nodeId: nodeId)
        }
        .onDisappear {
            fabHiddenBinding.wrappedValue = false
        }
    }

    // MARK: - Deleted View

    private var deletedView: some View {
        VStack(spacing: 16) {
            Image(systemName: "checkmark.circle.fill")
                .font(.system(size: 48))
                .foregroundColor(.green)
            Text("投稿を削除しました")
                .font(.headline)
            Button("戻る") {
                dismiss()
            }
            .buttonStyle(.bordered)
        }
    }

    // MARK: - Detail Content

    private func nodeDetailContent(node: Node) -> some View {
        VStack(spacing: 0) {
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    DetailHeaderSection(node: node)

                    DetailReactionBar(
                        node: node,
                        isAuthenticated: isAuthenticated,
                        onLoginRequired: loginRequired,
                        onToggleReaction: { type in viewModel.toggleReaction(type: type) }
                    )

                    DetailDerivationTreeView(
                        parentNode: node.parentNode,
                        childNodes: viewModel.childNodes
                    )

                    DetailCommentsView(
                        comments: viewModel.comments,
                        currentUserId: currentUserId,
                        editingCommentId: viewModel.editingCommentId,
                        editCommentText: viewModel.editCommentText,
                        onStartEditing: { comment in viewModel.startEditingComment(comment: comment) },
                        onCancelEditing: { viewModel.cancelEditingComment() },
                        onUpdateEditText: { text in viewModel.updateEditCommentText(text: text) },
                        onSaveEdit: { viewModel.saveCommentEdit() },
                        onRequestDelete: { commentId in
                            commentToDelete = commentId
                            showDeleteCommentAlert = true
                        }
                    )
                }
                .padding(16)
            }
            .refreshable {
                viewModel.refreshDetail(nodeId: nodeId)
            }

            commentInputBar
        }
        .toolbar(.hidden, for: .tabBar)
    }

    // MARK: - Comment Input Bar

    private var commentInputBar: some View {
        Group {
            if isAuthenticated {
                VStack(spacing: 0) {
                    Divider()
                    HStack(spacing: 8) {
                        TextField(
                            "コメントを入力...",
                            text: Binding(
                                get: { viewModel.commentText },
                                set: { viewModel.updateCommentText(text: $0) }
                            )
                        )
                        .textFieldStyle(.roundedBorder)

                        if viewModel.isCommentSubmitting {
                            ProgressView()
                                .frame(width: 44, height: 44)
                        } else {
                            Button(action: {
                                viewModel.submitComment()
                            }) {
                                Image(systemName: "paperplane.fill")
                                    .foregroundColor(.appPrimary)
                                    .frame(width: 44, height: 44)
                                    .contentShape(Rectangle())
                            }
                            .disabled(
                                viewModel.commentText.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
                            )
                            .accessibilityLabel("コメントを送信")
                        }
                    }
                    .padding(.horizontal, 16)
                    .padding(.vertical, 8)
                }
                .background(Color(.systemBackground))
            } else {
                VStack(spacing: 0) {
                    Divider()
                    Button(action: loginRequired) {
                        HStack {
                            Image(systemName: "lock.fill")
                                .font(.caption)
                            Text("ログインしてコメントする")
                                .font(.subheadline)
                        }
                        .foregroundColor(.appPrimary)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 10)
                    }
                    .padding(.horizontal, 16)
                    .padding(.vertical, 8)
                }
                .background(Color(.systemBackground))
            }
        }
    }
}

// NOTE: DetailView全体のPreviewはKoinHelper依存のため現状では動作しません。
// 個別コンポーネント（DetailHeaderSection, DetailReactionBar, DetailCommentsView）のPreviewは動作します。
// TODO: Preview用のMock ViewModelを作成して画面全体のPreviewを有効化

// #Preview("DetailView") {
//     NavigationStack {
//         DetailView(nodeId: "preview-1")
//     }
// }
