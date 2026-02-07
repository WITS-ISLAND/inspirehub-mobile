import KMPObservableViewModelSwiftUI

import Shared

import SwiftUI

// MARK: - Preview

struct DerivedPostView: View {
    let parentNode: Node
    @StateViewModel var viewModel = KoinHelper().getPostViewModel()
    @Environment(\.dismiss) private var dismiss

    @State private var tagInput: String = ""

    private var title: String { viewModel.title as? String ?? "" }
    private var content: String { viewModel.content as? String ?? "" }
    private var tags: [String] { viewModel.tags as? [String] ?? [] }
    private var isSubmitting: Bool { viewModel.isSubmitting as? Bool ?? false }
    private var error: String? { viewModel.error as? String }
    private var isSuccess: Bool { viewModel.isSuccess as? Bool ?? false }
    private var isValid: Bool { viewModel.isValid as? Bool ?? false }

    var body: some View {
        NavigationStack {
            Form {
                Section(header: Text("派生元")) {
                    HStack {
                        Image(systemName: NodeTypeStyle.icon(for: parentNode.type))
                            .foregroundColor(NodeTypeStyle.color(for: parentNode.type))
                        VStack(alignment: .leading, spacing: 2) {
                            Text(parentNode.title)
                                .font(.subheadline)
                                .fontWeight(.semibold)
                            Text(parentNode.content)
                                .font(.caption)
                                .foregroundColor(.secondary)
                                .lineLimit(2)
                        }
                    }
                    .padding(.vertical, 4)
                }

                Section(header: Text("タイトル")) {
                    TextField(
                        "派生アイデアのタイトルを入力",
                        text: Binding(
                            get: { title },
                            set: { viewModel.updateTitle(value: $0) }
                        ))
                }

                Section(header: Text("本文")) {
                    TextEditor(
                        text: Binding(
                            get: { content },
                            set: { viewModel.updateContent(value: $0) }
                        )
                    )
                    .frame(minHeight: 150)
                }

                Section(header: Text("タグ")) {
                    HStack {
                        TextField("タグを入力", text: $tagInput)
                            .onSubmit {
                                addTag()
                            }
                        Button(action: addTag) {
                            Image(systemName: "plus.circle.fill")
                                .foregroundColor(.blue)
                        }
                        .disabled(tagInput.trimmingCharacters(in: .whitespaces).isEmpty)
                    }

                    if !tags.isEmpty {
                        FlowLayout(tags: tags) { tag in
                            RemovableTagChip(text: tag) {
                                viewModel.removeTag(tag: tag)
                            }
                        }
                    }
                }

                if let error = error {
                    Section {
                        Text(error)
                            .foregroundColor(.red)
                            .font(.caption)
                    }
                }
            }
            .navigationTitle("派生アイデアを投稿")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("キャンセル") {
                        dismiss()
                    }
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("投稿") {
                        viewModel.submitDerived()
                    }
                    .disabled(!isValid || isSubmitting)
                }
            }
            .overlay {
                if isSubmitting {
                    ProgressView("投稿中...")
                        .padding()
                        .background(Color(.systemBackground).opacity(0.9))
                        .cornerRadius(12)
                }
            }
            .onAppear {
                viewModel.setParentNode(node: parentNode)
            }
            .onChange(of: isSuccess) { _, newValue in
                if newValue {
                    dismiss()
                }
            }
        }
    }

    private func addTag() {
        let trimmed = tagInput.trimmingCharacters(in: .whitespaces)
        guard !trimmed.isEmpty else { return }
        viewModel.addTag(tag: trimmed)
        tagInput = ""
    }
}
#Preview("DerivedPostView") {
    DerivedPostView(parentNode: PreviewData.sampleIssueNode)
}
