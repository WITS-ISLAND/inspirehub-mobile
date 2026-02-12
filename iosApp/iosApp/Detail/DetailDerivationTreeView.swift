import Shared

import SwiftUI

// MARK: - DetailDerivationTreeView

struct DetailDerivationTreeView: View {
    /// 親ノード情報（派生元）
    let parentNode: ParentNode?
    /// 子ノード一覧（派生先）
    let childNodes: [Node]

    private var hasParent: Bool { parentNode != nil }
    private var hasChildren: Bool { !childNodes.isEmpty }

    var body: some View {
        if hasParent || hasChildren {
            VStack(alignment: .leading, spacing: 0) {
                // Section header
                HStack(spacing: 6) {
                    Image(systemName: "arrow.triangle.branch")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                    Text("派生ツリー")
                        .font(.headline)
                }
                .padding(.bottom, 12)

                // Tree items
                VStack(alignment: .leading, spacing: 0) {
                    if let parentNode {
                        let isLast = !hasChildren
                        derivationTreeItem(
                            label: "派生元",
                            type: parentNode.type,
                            title: parentNode.title,
                            content: parentNode.content,
                            nodeId: parentNode.id,
                            isLast: isLast
                        )
                    }

                    ForEach(Array(childNodes.enumerated()), id: \.element.id) { index, child in
                        let isLast = index == childNodes.count - 1
                        derivationTreeItem(
                            label: "派生先",
                            type: child.type,
                            title: child.title,
                            content: child.content,
                            nodeId: child.id,
                            isLast: isLast
                        )
                    }
                }
            }
        }
    }

    // MARK: - Tree Item

    private func derivationTreeItem(
        label: String,
        type: NodeType,
        title: String,
        content: String?,
        nodeId: String,
        isLast: Bool
    ) -> some View {
        HStack(alignment: .top, spacing: 0) {
            // Tree connector line
            treeConnector(isLast: isLast)

            // Card
            NavigationLink(destination: DetailView(nodeId: nodeId)) {
                derivationCard(
                    label: label,
                    type: type,
                    title: title,
                    content: content
                )
            }
            .buttonStyle(.plain)
        }
    }

    // MARK: - Tree Connector

    private func treeConnector(isLast: Bool) -> some View {
        VStack(spacing: 0) {
            // Branch symbol: top vertical line + horizontal connector
            HStack(alignment: .top, spacing: 0) {
                // Vertical line (left side)
                Rectangle()
                    .fill(Color.secondary.opacity(0.3))
                    .frame(width: 2)

                // Horizontal connector
                Rectangle()
                    .fill(Color.secondary.opacity(0.3))
                    .frame(width: 12, height: 2)
                    .padding(.top, 18)
            }
            .frame(width: 14)

            // Continuation line below (only if not last)
            if !isLast {
                Rectangle()
                    .fill(Color.secondary.opacity(0.3))
                    .frame(width: 2)
                    .frame(maxHeight: .infinity)
                    .frame(width: 14, alignment: .leading)
            } else {
                Spacer()
                    .frame(width: 14)
            }
        }
        .padding(.trailing, 8)
    }

    // MARK: - Derivation Card

    private func derivationCard(
        label: String,
        type: NodeType,
        title: String,
        content: String?
    ) -> some View {
        HStack(spacing: 10) {
            Image(systemName: NodeTypeStyle.icon(for: type))
                .font(.title3)
                .foregroundColor(NodeTypeStyle.color(for: type))

            VStack(alignment: .leading, spacing: 4) {
                HStack(spacing: 4) {
                    Text(label)
                        .font(.caption2)
                        .foregroundColor(.secondary)
                    Text(NodeTypeStyle.label(for: type))
                        .font(.caption2)
                        .fontWeight(.medium)
                        .foregroundColor(NodeTypeStyle.color(for: type))
                }
                Text(title)
                    .font(.subheadline)
                    .fontWeight(.medium)
                    .foregroundColor(.primary)
                    .lineLimit(2)
                if let content, !content.isEmpty {
                    Text(content)
                        .font(.caption)
                        .foregroundStyle(.secondary)
                        .lineLimit(2)
                }
            }

            Spacer()

            Image(systemName: "chevron.right")
                .font(.caption)
                .foregroundColor(.secondary)
        }
        .padding(12)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(NodeTypeStyle.color(for: type).opacity(0.05))
        .cornerRadius(8)
        .padding(.vertical, 4)
    }
}

#Preview("DerivationTree - With Parent") {
    NavigationStack {
        DetailDerivationTreeView(
            parentNode: ParentNode(
                id: "parent-1",
                type: .issue,
                title: "サンプル課題",
                content: "これはサンプルの課題内容です。"
            ),
            childNodes: []
        )
        .padding(16)
    }
}

#Preview("DerivationTree - With Children") {
    NavigationStack {
        DetailDerivationTreeView(
            parentNode: nil,
            childNodes: [PreviewData.sampleNode, PreviewData.sampleDerivedNode]
        )
        .padding(16)
    }
}
