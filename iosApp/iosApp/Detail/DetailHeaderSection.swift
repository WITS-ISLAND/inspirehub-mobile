import Shared

import SwiftUI

// MARK: - DetailHeaderSection

struct DetailHeaderSection: View {
    /// 表示するノード情報
    let node: Node

    var body: some View {
        VStack(alignment: .leading, spacing: 20) {
            headerContent
            bodyContent

            if !node.tagIds.isEmpty {
                tagChips
            }

            metaContent
        }
    }

    // MARK: - Header

    private var headerContent: some View {
        VStack(alignment: .leading, spacing: 8) {
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

            Text(node.title)
                .font(.title2)
                .fontWeight(.bold)
        }
    }

    // MARK: - Body

    private var bodyContent: some View {
        Text(node.content)
            .font(.body)
            .lineSpacing(4)
    }

    // MARK: - Tags

    private var tagChips: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                ForEach(node.tagIds, id: \.self) { tagId in
                    Text("#\(tagId)")
                        .font(.caption)
                        .padding(.horizontal, 10)
                        .padding(.vertical, 5)
                        .background(Color.appPrimary.opacity(0.1))
                        .foregroundColor(.appPrimary)
                        .cornerRadius(8)
                }
            }
        }
    }

    // MARK: - Meta

    private var metaContent: some View {
        VStack(alignment: .leading, spacing: 4) {
            Divider()
            HStack(spacing: 6) {
                UserAvatarView(pictureURL: node.authorPicture, size: 16)
                Text(node.authorName)
                    .font(.caption)
                    .foregroundColor(.secondary)
                    .lineLimit(1)
                    .truncationMode(.middle)
            }
            Divider()
        }
    }
}

#Preview("DetailHeaderSection - Idea") {
    DetailHeaderSection(node: PreviewData.sampleNode)
        .padding(16)
}

#Preview("DetailHeaderSection - Issue") {
    DetailHeaderSection(node: PreviewData.sampleIssueNode)
        .padding(16)
}
