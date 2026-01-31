import SwiftUI
import Shared
import Combine

struct RootView: View {
    @StateObject private var viewModel = AuthViewModelWrapper()

    var body: some View {
        Group {
            if viewModel.isAuthenticated {
                HomeView()
            } else {
                LoginView(viewModel: viewModel)
            }
        }
    }
}

// AuthViewModel をSwiftUIで使えるようにラップ
class AuthViewModelWrapper: ObservableObject {
    private let viewModel: AuthViewModel
    private var cancellables = Set<AnyCancellable>()

    @Published var currentUser: User? = nil
    @Published var isAuthenticated: Bool = false
    @Published var isLoading: Bool = false
    @Published var error: String? = nil
    @Published var authUrl: String? = nil

    init() {
        // Koinから取得
        self.viewModel = KoinHelper().getAuthViewModel()

        // StateFlowを監視（KMP-ObservableViewModel使用）
        observeViewModel()
    }

    private func observeViewModel() {
        // Timer使ってポーリング（シンプルな方法）
        Timer.publish(every: 0.1, on: .main, in: .common)
            .autoconnect()
            .sink { [weak self] _ in
                guard let self = self else { return }
                self.currentUser = self.viewModel.currentUser.value as? User
                self.isAuthenticated = self.viewModel.isAuthenticated.value as! Bool
                self.isLoading = self.viewModel.isLoading.value as! Bool
                self.error = self.viewModel.error.value as? String
                self.authUrl = self.viewModel.authUrl.value as? String
            }
            .store(in: &cancellables)
    }

    func getGoogleAuthUrl() {
        viewModel.getGoogleAuthUrl()
    }

    func loginWithAuthCode(code: String) {
        viewModel.loginWithAuthCode(code: code)
    }

    func logout() {
        viewModel.logout()
    }

    func clearError() {
        viewModel.clearError()
    }
}
