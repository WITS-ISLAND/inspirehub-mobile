import SwiftUI

/// ユーザーアイコンを表示するコンポーネント
/// - pictureURL が nil または読み込み失敗時はフォールバックアイコンを表示
struct UserAvatarView: View {
    let pictureURL: String?
    let size: CGFloat

    var body: some View {
        Group {
            if let urlString = pictureURL, let url = URL(string: urlString) {
                AsyncImage(url: url) { phase in
                    switch phase {
                    case .empty:
                        ProgressView()
                            .frame(width: size, height: size)
                    case .success(let image):
                        image
                            .resizable()
                            .aspectRatio(contentMode: .fill)
                            .frame(width: size, height: size)
                            .clipShape(Circle())
                    case .failure:
                        fallbackIcon
                    @unknown default:
                        fallbackIcon
                    }
                }
            } else {
                fallbackIcon
            }
        }
    }

    private var fallbackIcon: some View {
        Image(systemName: "person.circle.fill")
            .resizable()
            .aspectRatio(contentMode: .fit)
            .frame(width: size, height: size)
            .foregroundColor(.appPrimary)
    }
}

#Preview("With URL") {
    UserAvatarView(
        pictureURL: "https://lh3.googleusercontent.com/a/default-user",
        size: 72
    )
}

#Preview("Without URL") {
    UserAvatarView(
        pictureURL: nil,
        size: 72
    )
}
