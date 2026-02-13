import KMPObservableViewModelSwiftUI
import Shared

import SwiftUI

// MARK: - DetailEditView

/// ノード編集画面
///
/// ノードのタイトル、本文、タグを編集するためのフォームを表示する。
/// タイプバッジは読み取り専用で表示される。
struct DetailEditView: View {
    /// 編集するノード情報
    let node: Node
    /// 編集中のタイトル（双方向バインディング）
    @Binding var editTitle: String
    /// 編集中の本文（双方向バインディング）
    @Binding var editContent: String
    /// 編集中のタグ（StateFlowから直接取得）
    let editTags: [String]
    /// タグ候補（StateFlowから直接取得）
    let editTagSuggestions: [Tag]
    /// タグ検索クエリ（StateFlowから直接取得）
    let editTagQuery: String
    /// タグ追加メソッド
    let onAddTag: (String) -> Void
    /// タグ削除メソッド
    let onRemoveTag: (String) -> Void
    /// タグ検索クエリ更新メソッド
    let onUpdateTagQuery: (String) -> Void
    /// タグ候補検索メソッド
    let onSearchTagSuggestions: (String) -> Void
    /// タグ候補クリアメソッド
    let onClearTagSuggestions: () -> Void
    /// エラーメッセージ
    let error: String?
    /// 読み込み中フラグ
    let isLoading: Bool

    @State private var tagInput: String = ""

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                // Type badge (read-only)
                HStack(spacing: 8) {
                    Image(systemName: NodeTypeStyle.icon(for: node.type))
                        .foregroundColor(NodeTypeStyle.color(for: node.type))
                    Text(NodeTypeStyle.label(for: node.type))
                        .font(.caption)
                        .fontWeight(.semibold)
                        .foregroundColor(NodeTypeStyle.color(for: node.type))
                        .padding(.horizontal, 8)
                        .padding(.vertical, 2)
                        .background(NodeTypeStyle.backgroundColor(for: node.type))
                        .cornerRadius(4)
                }

                VStack(alignment: .leading, spacing: 8) {
                    Text("タイトル")
                        .font(.subheadline)
                        .fontWeight(.semibold)
                        .foregroundColor(.secondary)
                    TextField("タイトル", text: $editTitle)
                        .textFieldStyle(.roundedBorder)
                        .font(.body)
                }

                VStack(alignment: .leading, spacing: 8) {
                    Text("内容")
                        .font(.subheadline)
                        .fontWeight(.semibold)
                        .foregroundColor(.secondary)
                    TextEditor(text: $editContent)
                        .frame(minHeight: 200)
                        .overlay(
                            RoundedRectangle(cornerRadius: 8)
                                .stroke(Color(.systemGray4), lineWidth: 1)
                        )
                }

                // タグ編集セクション
                VStack(alignment: .leading, spacing: 8) {
                    Text("タグ")
                        .font(.subheadline)
                        .fontWeight(.semibold)
                        .foregroundColor(.secondary)

                    // タグ入力フィールド
                    HStack {
                        TextField("タグを入力", text: $tagInput)
                            .textFieldStyle(.roundedBorder)
                            .onChange(of: tagInput) { _, newValue in
                                onUpdateTagQuery(newValue)
                                onSearchTagSuggestions(newValue)
                            }
                            .onSubmit {
                                addTag()
                            }
                        Button(action: addTag) {
                            Image(systemName: "plus.circle.fill")
                                .foregroundColor(.appPrimary)
                        }
                        .disabled(tagInput.trimmingCharacters(in: .whitespaces).isEmpty)
                    }

                    // タグ候補表示
                    if !editTagSuggestions.isEmpty {
                        ScrollView(.horizontal, showsIndicators: false) {
                            HStack(spacing: 8) {
                                ForEach(editTagSuggestions, id: \.id) { tag in
                                    Button(action: {
                                        onAddTag(tag.name)
                                        tagInput = ""
                                        onClearTagSuggestions()
                                    }) {
                                        HStack(spacing: 4) {
                                            Text("#\(tag.name)")
                                                .font(.caption)
                                            Text("(\(tag.usageCount))")
                                                .font(.caption2)
                                                .foregroundColor(.secondary)
                                        }
                                        .padding(.horizontal, 10)
                                        .padding(.vertical, 6)
                                        .background(Color.appPrimary.opacity(0.1))
                                        .foregroundColor(.appPrimary)
                                        .cornerRadius(8)
                                    }
                                    .buttonStyle(.plain)
                                }
                            }
                        }
                    }

                    // 選択済みタグ表示
                    if !editTags.isEmpty {
                        FlowLayout(tags: editTags) { tag in
                            RemovableTagChip(text: tag) {
                                onRemoveTag(tag)
                            }
                        }
                    }
                }

                if let error {
                    Text(error)
                        .font(.caption)
                        .foregroundColor(.red)
                }

                if isLoading {
                    HStack {
                        Spacer()
                        ProgressView()
                        Spacer()
                    }
                }
            }
            .padding(16)
        }
    }

    private func addTag() {
        let trimmed = tagInput.trimmingCharacters(in: .whitespaces)
        guard !trimmed.isEmpty else { return }
        onAddTag(trimmed)
        tagInput = ""
        onClearTagSuggestions()
    }
}

#Preview("DetailEditView") {
    DetailEditView(
        node: PreviewData.sampleNode,
        editTitle: .constant("サンプルタイトル"),
        editContent: .constant("サンプル内容"),
        editTags: ["Swift", "iOS"],
        editTagSuggestions: [],
        editTagQuery: "",
        onAddTag: { _ in },
        onRemoveTag: { _ in },
        onUpdateTagQuery: { _ in },
        onSearchTagSuggestions: { _ in },
        onClearTagSuggestions: {},
        error: nil,
        isLoading: false
    )
}

#Preview("DetailEditView - Error") {
    DetailEditView(
        node: PreviewData.sampleNode,
        editTitle: .constant(""),
        editContent: .constant(""),
        editTags: [],
        editTagSuggestions: [],
        editTagQuery: "",
        onAddTag: { _ in },
        onRemoveTag: { _ in },
        onUpdateTagQuery: { _ in },
        onSearchTagSuggestions: { _ in },
        onClearTagSuggestions: {},
        error: "保存に失敗しました",
        isLoading: false
    )
}
