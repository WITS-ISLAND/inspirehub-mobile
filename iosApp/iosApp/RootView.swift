import KMPObservableViewModelSwiftUI
import Shared
import SwiftUI

// MARK: - Auth Environment

private struct IsAuthenticatedKey: EnvironmentKey {
    static let defaultValue: Bool = false
}

private struct LoginRequiredActionKey: EnvironmentKey {
    static let defaultValue: () -> Void = {}
}

private struct CurrentUserIdKey: EnvironmentKey {
    static let defaultValue: String? = nil
}

extension EnvironmentValues {
    var isAuthenticated: Bool {
        get { self[IsAuthenticatedKey.self] }
        set { self[IsAuthenticatedKey.self] = newValue }
    }

    var loginRequired: () -> Void {
        get { self[LoginRequiredActionKey.self] }
        set { self[LoginRequiredActionKey.self] = newValue }
    }

    var currentUserId: String? {
        get { self[CurrentUserIdKey.self] }
        set { self[CurrentUserIdKey.self] = newValue }
    }
}

// MARK: - RootView

struct RootView: View {
    @StateViewModel var viewModel = KoinHelper().getAuthViewModel()
    @State private var showLoginSheet = false

    private var isAuth: Bool {
        viewModel.isAuthenticated as? Bool ?? false
    }

    var body: some View {
        MainTabView(
            isAuthenticated: isAuth,
            onLoginRequired: {
                showLoginSheet = true
            }
        )
        .environment(\.isAuthenticated, isAuth)
        .environment(\.currentUserId, (viewModel.currentUser as? User)?.id)
        .environment(\.loginRequired, { showLoginSheet = true })
        .sheet(isPresented: $showLoginSheet) {
            NavigationStack {
                LoginView(viewModel: viewModel)
                    .toolbar {
                        ToolbarItem(placement: .navigationBarLeading) {
                            Button("閉じる") {
                                showLoginSheet = false
                            }
                        }
                    }
            }
        }
        .onChange(of: isAuth) { _, newIsAuth in
            if newIsAuth {
                showLoginSheet = false
            }
        }
    }
}

// MARK: - Preview

#Preview("RootView") {
    RootView()
}
