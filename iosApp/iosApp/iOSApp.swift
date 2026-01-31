import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        // Koin初期化
        KoinInitializerKt.doInitKoin(appDeclaration: { _ in })
    }

    var body: some Scene {
        WindowGroup {
            RootView()
        }
    }
}