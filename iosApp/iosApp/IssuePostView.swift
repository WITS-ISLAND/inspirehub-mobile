import SwiftUI

struct IssuePostView: View {
    @StateObject private var viewModel = PostViewModelWrapper()
    @Environment(\.dismiss) private var dismiss

    @State private var tagInput: String = ""

    var body: some View {
        NavigationView {
            Form {
                Section(header: Text("タイトル")) {
                    TextField("課題のタイトルを入力", text: Binding(
                        get: { viewModel.title },
                        set: { viewModel.updateTitle($0) }
                    ))
                }

                Section(header: Text("本文")) {
                    TextEditor(text: Binding(
                        get: { viewModel.content },
                        set: { viewModel.updateContent($0) }
                    ))
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

                    if !viewModel.tags.isEmpty {
                        FlowLayout(tags: viewModel.tags) { tag in
                            TagChip(text: tag)
                        }
                    }
                }

                if let error = viewModel.error {
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
                    .disabled(viewModel.title.trimmingCharacters(in: .whitespaces).isEmpty || viewModel.isSubmitting)
                }
            }
            .overlay {
                if viewModel.isSubmitting {
                    ProgressView("投稿中...")
                        .padding()
                        .background(Color(.systemBackground).opacity(0.9))
                        .cornerRadius(12)
                }
            }
            .onChange(of: viewModel.isSuccess) { newValue in
                if newValue {
                    dismiss()
                }
            }
        }
    }

    private func addTag() {
        let trimmed = tagInput.trimmingCharacters(in: .whitespaces)
        guard !trimmed.isEmpty else { return }
        viewModel.addTag(trimmed)
        tagInput = ""
    }
}

struct TagChip: View {
    let text: String

    var body: some View {
        Text(text)
            .font(.caption)
            .padding(.horizontal, 10)
            .padding(.vertical, 5)
            .background(Color.blue.opacity(0.1))
            .foregroundColor(.blue)
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

// MARK: - Preview

#Preview("IssuePostView") {
    IssuePostView()
}
