import SwiftUI

private enum PostType {
    case issue, idea
}

struct MainTabView: View {
    @State private var selectedTab = 0
    @State private var showPostTypeSheet = false
    @State private var showIssuePost = false
    @State private var showIdeaPost = false
    @State private var pendingPostType: PostType?

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
                    MyPageView()
                }
                .tabItem {
                    Image(systemName: "person.fill")
                    Text("マイページ")
                }
                .tag(2)
            }

            // FAB
            Button(action: {
                showPostTypeSheet = true
            }) {
                Image(systemName: "plus")
                    .font(.title2)
                    .fontWeight(.bold)
                    .foregroundColor(.white)
                    .frame(width: 56, height: 56)
                    .background(Color.blue)
                    .clipShape(Circle())
                    .shadow(color: .black.opacity(0.2), radius: 4, x: 0, y: 2)
            }
            .padding(.trailing, 20)
            .padding(.bottom, 80)
        }
        .sheet(isPresented: $showPostTypeSheet, onDismiss: {
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
        }) {
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
    }
}

// MARK: - Preview

#Preview("MainTabView") {
    MainTabView()
}

#Preview("PostTypeSelectSheet") {
    PostTypeSelectSheet(
        onIssueSelected: {},
        onIdeaSelected: {}
    )
}
