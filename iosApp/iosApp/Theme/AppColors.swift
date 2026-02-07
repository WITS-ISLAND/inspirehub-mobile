import SwiftUI

/// Centralized color definitions for InspireHub.
/// All screens must reference these instead of hard-coding Color literals.
extension Color {
    // MARK: - Brand

    /// Primary brand color (blue-indigo) used for key actions and active indicators.
    static let appPrimary = Color(red: 0.25, green: 0.42, blue: 0.88)

    /// Secondary accent color (warm orange) used for creative / derive actions.
    static let appSecondary = Color(red: 0.95, green: 0.55, blue: 0.25)

    // MARK: - Node Types

    /// Issue nodes: warm coral-red conveying urgency.
    static let issueColor = Color(red: 0.90, green: 0.30, blue: 0.25)

    /// Idea nodes: indigo-blue conveying creativity.
    static let ideaColor = Color(red: 0.25, green: 0.42, blue: 0.88)

    /// Project nodes: teal-green conveying growth and progress.
    static let projectColor = Color(red: 0.18, green: 0.72, blue: 0.55)
}
