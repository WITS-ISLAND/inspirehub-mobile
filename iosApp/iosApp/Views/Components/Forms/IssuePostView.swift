import KMPObservableViewModelSwiftUI

import Shared

import SwiftUI

// MARK: - Removable Tag Chip

// MARK: - Preview

struct IssuePostView: View {
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
    private var suggestedTags: [Tag] { viewModel.suggestedTags as? [Tag] ?? [] }

    var body: some View {
        NavigationStack {
            Form {
                Section(header: Text("タイトル")) {
                    TextField(
                        "課題のタイトルを入力",
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
                            .onChange(of: tagInput) { _, newValue in
                                viewModel.searchTagSuggestions(query: newValue)
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

                    if !suggestedTags.isEmpty {
                        ScrollView(.horizontal, showsIndicators: false) {
                            HStack(spacing: 8) {
                                ForEach(suggestedTags, id: \.id) { tag in
                                    Button(action: {
                                        viewModel.addTag(tag: tag.name)
                                        tagInput = ""
                                        viewModel.clearTagSuggestions()
                                    }) {
                                        Text("#\(tag.name)")
                                            .font(.caption)
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
            .navigationTitle("課題を投稿")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("キャンセル") {
                        dismiss()
                    }
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("投稿") {
                        viewModel.submitIssue()
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
        viewModel.clearTagSuggestions()
    }
}
struct RemovableTagChip: View {
    let text: String
    let onRemove: () -> Void

    var body: some View {
        HStack(spacing: 4) {
            Text(text)
                .font(.caption)
            Button(action: onRemove) {
                Image(systemName: "xmark.circle.fill")
                    .font(.caption2)
                    .foregroundColor(.blue.opacity(0.6))
            }
        }
        .padding(.horizontal, 10)
        .padding(.vertical, 5)
        .background(Color.appPrimary.opacity(0.1))
        .foregroundColor(.appPrimary)
        .cornerRadius(8)
    }
}
struct TagChip: View {
    let text: String

    var body: some View {
        Text(text)
            .font(.caption)
            .padding(.horizontal, 10)
            .padding(.vertical, 5)
            .background(Color.appPrimary.opacity(0.1))
            .foregroundColor(.appPrimary)
            .cornerRadius(8)
    }
}
struct FlowLayout<Data: RandomAccessCollection, Content: View>: View where Data.Element: Hashable {
    let tags: Data
    let content: (Data.Element) -> Content

    var body: some View {
        LazyVGrid(columns: [GridItem(.adaptive(minimum: 80), spacing: 8)], spacing: 8) {
            ForEach(Array(tags.enumerated()), id: \.offset) { _, tag in
                content(tag)
            }
        }
    }
}

// NOTE: IssuePostView全体のPreviewはKoinHelper依存のため現状では動作しません。
// TODO: Preview用のMock ViewModelを作成して画面全体のPreviewを有効化

// #Preview("IssuePostView") {
//     IssuePostView()
// }
