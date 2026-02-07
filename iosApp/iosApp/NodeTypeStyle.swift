import SwiftUI
import Shared

/// Provides consistent styling for node types across all screens.
/// Centralizes color, icon, and label definitions to prevent inconsistencies.
enum NodeTypeStyle {

    // MARK: - Colors

    /// The foreground/accent color for a given node type.
    ///
    /// - Issue: Red -- conveys urgency and problem-awareness
    /// - Idea: Blue -- aligns with the app's Primary color, conveys creativity
    /// - Project: Green -- conveys progress and growth
    static func color(for type: NodeType) -> Color {
        switch type {
        case .issue: return .red
        case .idea: return .blue
        case .project: return .green
        default: return .secondary
        }
    }

    /// A translucent background tint for badges.
    static func backgroundColor(for type: NodeType) -> Color {
        color(for: type).opacity(0.12)
    }

    // MARK: - Icons (SF Symbols)

    /// The SF Symbol name for a given node type.
    static func icon(for type: NodeType) -> String {
        switch type {
        case .issue: return "exclamationmark.triangle.fill"
        case .idea: return "lightbulb.fill"
        case .project: return "folder.fill"
        default: return "doc.fill"
        }
    }

    // MARK: - Labels

    /// The localized display label for a given node type.
    static func label(for type: NodeType) -> String {
        switch type {
        case .issue: return "課題"
        case .idea: return "アイデア"
        case .project: return "プロジェクト"
        default: return ""
        }
    }
}
