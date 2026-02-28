import Shared

import SwiftUI

// MARK: - DetailReactionBar

/// ノード詳細のリアクションバー
///
/// いいね・気になる・やってみたいの3種類のリアクションボタンと、
/// 派生投稿ボタンを表示する。
/// タップでリアクションを切り替え、長押しでリアクションしたユーザー一覧シートを表示する。
struct DetailReactionBar: View {
    /// 表示するノード情報
    let node: Node
    /// ログイン状態
    let isAuthenticated: Bool
    /// ログイン要求時のコールバック
    let onLoginRequired: () -> Void
    /// リアクション切り替え時のコールバック
    let onToggleReaction: (ReactionType) -> Void
    /// リアクションユーザー一覧を表示するコールバック
    let onShowReactionUsers: (ReactionType) -> Void
    /// 派生投稿シート表示フラグ
    @State private var showDerivedPost = false

    var body: some View {
        VStack(spacing: 12) {
            reactionButtons
            deriveButton
        }
    }

    // MARK: - Reaction Buttons

    private var reactionButtons: some View {
        HStack(spacing: 16) {
            ReactionChipButton(
                emoji: "👍",
                label: "いいね",
                count: node.reactions.like.count,
                isReacted: node.reactions.like.isReacted,
                onTap: {
                    guard isAuthenticated else { onLoginRequired(); return }
                    onToggleReaction(.like)
                },
                onLongPress: {
                    guard node.reactions.like.count > 0 else { return }
                    onShowReactionUsers(.like)
                }
            )

            ReactionChipButton(
                emoji: "🔥",
                label: "気になる",
                count: node.reactions.interested.count,
                isReacted: node.reactions.interested.isReacted,
                onTap: {
                    guard isAuthenticated else { onLoginRequired(); return }
                    onToggleReaction(.interested)
                },
                onLongPress: {
                    guard node.reactions.interested.count > 0 else { return }
                    onShowReactionUsers(.interested)
                }
            )

            ReactionChipButton(
                emoji: "💪",
                label: "やってみたい",
                count: node.reactions.wantToTry.count,
                isReacted: node.reactions.wantToTry.isReacted,
                onTap: {
                    guard isAuthenticated else { onLoginRequired(); return }
                    onToggleReaction(.wantToTry)
                },
                onLongPress: {
                    guard node.reactions.wantToTry.count > 0 else { return }
                    onShowReactionUsers(.wantToTry)
                }
            )
        }
        .padding(.vertical, 4)
    }

    // MARK: - Derive Button

    private var deriveButton: some View {
        Button(action: {
            guard isAuthenticated else {
                onLoginRequired()
                return
            }
            showDerivedPost = true
        }) {
            HStack {
                Image(systemName: "plus.bubble")
                Text("派生アイデアを投稿")
            }
            .font(.subheadline)
            .fontWeight(.semibold)
            .foregroundColor(.white)
            .frame(maxWidth: .infinity)
            .padding(12)
            .background(Color.appSecondary)
            .cornerRadius(10)
        }
        .sheet(isPresented: $showDerivedPost) {
            DerivedPostView(parentNode: node)
        }
    }
}

// MARK: - ReactionChipButton

/// タップアニメーション付きリアクションチップボタン
///
/// タップ時にスプリングバウンスアニメーションを再生し、
/// リアクション状態の切り替えをイーズインアウトでスムーズに表示する。
/// 触覚フィードバック（iOS 17+）も提供する。
private struct ReactionChipButton: View {
    /// 絵文字テキスト
    let emoji: String
    /// ラベルテキスト
    let label: String
    /// リアクション数
    let count: Int32
    /// リアクション済みかどうか
    let isReacted: Bool
    /// タップ時のコールバック
    let onTap: () -> Void
    /// 長押し時のコールバック
    let onLongPress: () -> Void

    /// タップ中スケールアニメーション用フラグ
    @State private var isPressed = false

    var body: some View {
        VStack(spacing: 4) {
            // チップ: 絵文字（+ カウント）を常にカプセル表示
            HStack(spacing: 2) {
                Text(emoji)
                    .font(.caption)
                if count > 0 {
                    Text("\(count)")
                        .font(.caption.bold())
                }
            }
            .padding(.horizontal, 8)
            .padding(.vertical, 3)
            .foregroundColor(isReacted ? .blue : .secondary)
            .background(isReacted ? Color.blue.opacity(0.12) : Color.secondary.opacity(0.1))
            .clipShape(Capsule())

            Text(label)
                .font(.system(size: 10))
                .foregroundColor(isReacted ? .blue : .secondary)
        }
        .frame(minWidth: 60, minHeight: 44)
        .contentShape(Rectangle())
        // スプリングバウンスアニメーション
        .scaleEffect(isPressed ? 1.25 : 1.0)
        // リアクション状態切り替え時のカラートランジション（0.2秒）
        .animation(.easeInOut(duration: 0.2), value: isReacted)
        // 触覚フィードバック（iOS 17+）
        .sensoryFeedback(.impact(flexibility: .soft, intensity: 0.7), trigger: isPressed)
        // タップ: スプリングバウンス → リアクション切り替え
        .onTapGesture {
            withAnimation(.spring(response: 0.3, dampingFraction: 0.5)) {
                isPressed = true
            }
            Task { @MainActor in
                try? await Task.sleep(nanoseconds: 100_000_000)
                withAnimation(.spring(response: 0.3, dampingFraction: 0.5)) {
                    isPressed = false
                }
            }
            onTap()
        }
        // 長押し: ユーザー一覧シートを表示（count > 0 のときのみ）
        .onLongPressGesture {
            onLongPress()
        }
        .accessibilityLabel(
            count > 0
                ? "\(label) \(count) 長押しでユーザー一覧を表示"
                : "\(label) タップでリアクション追加"
        )
    }
}

#Preview("DetailReactionBar") {
    DetailReactionBar(
        node: PreviewData.sampleNode,
        isAuthenticated: true,
        onLoginRequired: {},
        onToggleReaction: { _ in },
        onShowReactionUsers: { _ in }
    )
    .padding(16)
}

#Preview("DetailReactionBar - Not Authenticated") {
    DetailReactionBar(
        node: PreviewData.sampleIssueNode,
        isAuthenticated: false,
        onLoginRequired: {},
        onToggleReaction: { _ in },
        onShowReactionUsers: { _ in }
    )
    .padding(16)
}
