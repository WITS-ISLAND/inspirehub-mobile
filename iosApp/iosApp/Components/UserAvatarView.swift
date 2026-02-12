import SwiftUI

/// ユーザーアイコンを表示する再利用可能なコンポーネント
///
/// GoogleアイコンのURLから画像を読み込み、失敗時にはフォールバックアイコンを表示します。
struct UserAvatarView: View {
    let pictureURL: String?
    let size: CGFloat

    init(pictureURL: String?, size: CGFloat = 40) {
        self.pictureURL = pictureURL
        self.size = size
    }

    var body: some View {
        Group {
            if let urlString = pictureURL,
               let url = URL(string: urlString) {
                AsyncImage(url: url) { phase in
                    switch phase {
                    case .empty:
                        ProgressView()
                    case .success(let image):
                        image
                            .resizable()
                            .scaledToFill()
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
        .frame(width: size, height: size)
        .clipShape(Circle())
    }

    private var fallbackIcon: some View {
        Image(systemName: "person.circle.fill")
            .resizable()
            .scaledToFit()
            .foregroundColor(.appPrimary)
    }
}

#Preview("UserAvatarView - With Image") {
    UserAvatarView(
        pictureURL: "https://lh3.googleusercontent.com/a/default-user",
        size: 72
    )
}

#Preview("UserAvatarView - Fallback") {
    UserAvatarView(
        pictureURL: nil,
        size: 72
    )
}
