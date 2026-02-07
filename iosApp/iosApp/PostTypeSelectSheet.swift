import SwiftUI

struct PostTypeSelectSheet: View {
    var onIssueSelected: () -> Void
    var onIdeaSelected: () -> Void

    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            VStack(spacing: 20) {
                Text("投稿タイプを選択")
                    .font(.title2)
                    .fontWeight(.bold)
                    .padding(.top, 24)

                Button(action: {
                    dismiss()
                    onIssueSelected()
                }) {
                    HStack {
                        Image(systemName: "exclamationmark.triangle.fill")
                            .font(.title2)
                            .foregroundColor(.orange)
                        VStack(alignment: .leading, spacing: 4) {
                            Text("課題を投稿")
                                .font(.headline)
                                .foregroundColor(.primary)
                            Text("解決したい問題や作りたいものを投稿")
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                        Spacer()
                        Image(systemName: "chevron.right")
                            .foregroundColor(.secondary)
                    }
                    .padding()
                    .background(Color(.systemGray6))
                    .cornerRadius(12)
                }
                .padding(.horizontal, 16)

                Button(action: {
                    dismiss()
                    onIdeaSelected()
                }) {
                    HStack {
                        Image(systemName: "lightbulb.fill")
                            .font(.title2)
                            .foregroundColor(.yellow)
                        VStack(alignment: .leading, spacing: 4) {
                            Text("アイデアを投稿")
                                .font(.headline)
                                .foregroundColor(.primary)
                            Text("解決策やひらめきを投稿")
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                        Spacer()
                        Image(systemName: "chevron.right")
                            .foregroundColor(.secondary)
                    }
                    .padding()
                    .background(Color(.systemGray6))
                    .cornerRadius(12)
                }
                .padding(.horizontal, 16)

                Spacer()
            }
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("閉じる") {
                        dismiss()
                    }
                }
            }
        }
        .presentationDetents([.medium])
    }
}

// MARK: - Preview

#Preview("PostTypeSelectSheet") {
    PostTypeSelectSheet(
        onIssueSelected: {},
        onIdeaSelected: {}
    )
}
