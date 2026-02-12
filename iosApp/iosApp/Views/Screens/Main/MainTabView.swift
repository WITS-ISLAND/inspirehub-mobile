import SwiftUI

// MARK: - Preview

private enum PostType {
    case issue, idea
}
struct MainTabView: View {
    let isAuthenticated: Bool
    let onLoginRequired: () -> Void

    @State private var selectedTab = 0
    @State private var showPostTypeSheet = false
    @State private var showIssuePost = false
    @State private var showIdeaPost = false
    @State private var pendingPostType: PostType?
    @State private var pendingPostAfterLogin = false
    @State private var isFABHidden = false

    var body: some View {
        ZStack(alignment: .bottomTrailing) {
            TabView(selection: $selectedTab) {
                NavigationStack {
                    HomeView()
                }
                .tabItem {
                    Image(systemName: "house.fill")
                    Text("ホーム")
                }
                .tag(0)

                NavigationStack {
                    DiscoverView()
                }
                .tabItem {
                    Image(systemName: "magnifyingglass")
                    Text("ディスカバー")
                }
                .tag(1)

                NavigationStack {
                    if isAuthenticated {
                        MyPageView()
                    } else {
                        loginPromptTab
                    }
                }
                .tabItem {
                    Image(systemName: "person.fill")
                    Text("マイページ")
                }
                .tag(2)
            }
            .environment(\.fabHiddenBinding, $isFABHidden)

            // FAB
            if !isFABHidden {
                Button(action: {
                    if isAuthenticated {
                        showPostTypeSheet = true
                    } else {
                        pendingPostAfterLogin = true
                        onLoginRequired()
                    }
                }) {
                    Image(systemName: "plus")
                        .font(.title2)
                        .fontWeight(.bold)
                        .foregroundColor(.white)
                        .frame(width: 56, height: 56)
                        .background(Color.appPrimary)
                        .clipShape(Circle())
                        .shadow(color: .black.opacity(0.2), radius: 4, x: 0, y: 2)
                }
                .accessibilityLabel("新規投稿")
                .padding(.trailing, 20)
                .padding(.bottom, 80)
            }
        }
        .sheet(
            isPresented: $showPostTypeSheet,
            onDismiss: {
                switch pendingPostType {
                case .issue:
                    pendingPostType = nil
                    showIssuePost = true
                case .idea:
                    pendingPostType = nil
                    showIdeaPost = true
                case nil:
                    break
                }
            }
        ) {
            PostTypeSelectSheet(
                onIssueSelected: { pendingPostType = .issue },
                onIdeaSelected: { pendingPostType = .idea }
            )
        }
        .fullScreenCover(isPresented: $showIssuePost) {
            IssuePostView()
        }
        .fullScreenCover(isPresented: $showIdeaPost) {
            IdeaPostView()
        }
        .onChange(of: isAuthenticated) { _, newValue in
            if newValue && pendingPostAfterLogin {
                pendingPostAfterLogin = false
                // ログインシートのdismissアニメーション完了を待ってから表示
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.6) {
                    showPostTypeSheet = true
                }
            }
        }
        .onChange(of: selectedTab) { _, _ in
            pendingPostAfterLogin = false
        }
    }

    private var loginPromptTab: some View {
        VStack(spacing: 16) {
            Image(systemName: "person.crop.circle.badge.questionmark")
                .font(.system(size: 60))
                .foregroundColor(.secondary)

            Text("ログインして利用する")
                .font(.title3)
                .fontWeight(.semibold)

            Text("マイページを利用するにはログインが必要です")
                .font(.subheadline)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)

            Button(action: onLoginRequired) {
                Text("ログイン")
                    .font(.headline)
                    .foregroundColor(.white)
                    .frame(maxWidth: 200)
                    .padding(.vertical, 12)
                    .background(Color.appPrimary)
                    .cornerRadius(12)
            }
        }
        .padding()
    }
}
#Preview("MainTabView") {
    MainTabView(isAuthenticated: true, onLoginRequired: {})
}
#Preview("MainTabView - Unauthenticated") {
    MainTabView(isAuthenticated: false, onLoginRequired: {})
}
#Preview("PostTypeSelectSheet") {
    PostTypeSelectSheet(
        onIssueSelected: {},
        onIdeaSelected: {}
    )
}
