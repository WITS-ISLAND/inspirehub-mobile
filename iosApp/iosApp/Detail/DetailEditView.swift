import Shared

import SwiftUI

// MARK: - DetailEditView

struct DetailEditView: View {
    /// 編集するノード情報
    let node: Node
    /// 編集中のタイトル（双方向バインディング）
    @Binding var editTitle: String
    /// 編集中の本文（双方向バインディング）
    @Binding var editContent: String
    /// エラーメッセージ
    let error: String?
    /// 読み込み中フラグ
    let isLoading: Bool

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
}

#Preview("DetailEditView") {
    DetailEditView(
        node: PreviewData.sampleNode,
        editTitle: .constant("サンプルタイトル"),
        editContent: .constant("サンプル内容"),
        error: nil,
        isLoading: false
    )
}

#Preview("DetailEditView - Error") {
    DetailEditView(
        node: PreviewData.sampleNode,
        editTitle: .constant(""),
        editContent: .constant(""),
        error: "保存に失敗しました",
        isLoading: false
    )
}
