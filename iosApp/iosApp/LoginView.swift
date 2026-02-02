import SwiftUI
import Shared
import GoogleSignIn
import GoogleSignInSwift
import KMPObservableViewModelSwiftUI

struct LoginView: View {
    @ObservedViewModel var viewModel: AuthViewModel
    @State private var signInError: String?

    var body: some View {
        VStack(spacing: 32) {
            Spacer()

            // ロゴ・タイトル
            VStack(spacing: 16) {
                Image(systemName: "lightbulb.fill")
                    .font(.system(size: 80))
                    .foregroundColor(.orange)

                Text("InspireHub")
                    .font(.largeTitle)
                    .fontWeight(.bold)

                Text("社内ハッカソンをもっと楽しく")
                    .font(.subheadline)
                    .foregroundColor(.secondary)
            }

            Spacer()

            // Googleログインボタン（SDK版）
            Button(action: handleGoogleSignIn) {
                HStack {
                    Image(systemName: "g.circle.fill")
                    Text("Googleでログイン")
                }
                .font(.headline)
                .foregroundColor(.white)
                .frame(maxWidth: .infinity)
                .padding()
                .background(Color.blue)
                .cornerRadius(12)
            }
            .disabled(viewModel.isLoading)
            .padding(.horizontal, 32)

            // ローディング表示
            if viewModel.isLoading {
                ProgressView()
                    .padding(.top, 8)
            }

            // エラー表示（VM側 + クライアント側）
            if let error = viewModel.error ?? signInError {
                Text(error)
                    .font(.caption)
                    .foregroundColor(.red)
                    .padding(.horizontal, 32)
                    .padding(.top, 8)
            }

            Spacer()
        }
    }

    private func handleGoogleSignIn() {
        signInError = nil

        #if DEBUG
        // Phase1: モック認証（Google OAuth未設定のため）
        viewModel.mockLogin()
        return
        #endif

        // === 以下は本来のGoogle Sign-In処理（そのまま残す） ===
        print("=== Google Sign-In Started ===")

        guard let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
              let rootViewController = windowScene.windows.first?.rootViewController else {
            print("Error: Could not find root view controller")
            return
        }

        GIDSignIn.sharedInstance.signIn(withPresenting: rootViewController) { result, error in
            if let error = error {
                print("Google Sign-In Error: \(error.localizedDescription)")
                signInError = error.localizedDescription
                return
            }

            guard let user = result?.user,
                  let idToken = user.idToken?.tokenString else {
                print("Error: Could not get ID token")
                signInError = "認証情報の取得に失敗しました"
                return
            }

            print("=== Google Sign-In Success ===")
            print("ID Token: \(idToken.prefix(20))...")
            print("User Email: \(user.profile?.email ?? "unknown")")

            // ViewModelでID tokenをバックエンドに送信
            viewModel.verifyGoogleToken(idToken: idToken)
        }
    }
}

// MARK: - Preview

#Preview("LoginView") {
    LoginView(viewModel: KoinHelper().getAuthViewModel())
}
