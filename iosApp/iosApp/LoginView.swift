import GoogleSignIn
import GoogleSignInSwift
import KMPObservableViewModelSwiftUI
import Shared
import SwiftUI

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
                    .foregroundColor(.appSecondary)

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
                .background(Color.appPrimary)
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

            Button("テスト用ログイン（DEV）") {
                viewModel.mockLogin()
            }
            .font(.caption)
            .foregroundColor(.secondary)

            Spacer()
        }
    }

    private func handleGoogleSignIn() {
        signInError = nil

        guard let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
            let rootViewController = windowScene.windows.first?.rootViewController
        else {
            signInError = "画面の取得に失敗しました"
            return
        }

        GIDSignIn.sharedInstance.signIn(withPresenting: rootViewController) { result, error in
            if let error = error as? NSError {
                // ユーザーがキャンセルした場合はエラー表示しない
                if error.domain == "com.google.GIDSignIn" && error.code == -5 {
                    return
                }
                signInError = error.localizedDescription
                return
            }

            guard let idToken = result?.user.idToken?.tokenString else {
                signInError = "認証情報の取得に失敗しました"
                return
            }

            // バックエンドでID tokenを検証してログイン
            viewModel.verifyGoogleToken(idToken: idToken)
        }
    }
}

// MARK: - Preview

#Preview("LoginView") {
    LoginView(viewModel: KoinHelper().getAuthViewModel())
}
