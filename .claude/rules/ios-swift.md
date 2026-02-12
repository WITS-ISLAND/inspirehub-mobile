---
description: Rules for editing Swift/SwiftUI files in the iOS app
globs: ["iosApp/**/*.swift"]
---

# iOS Swift 開発ルール

## ターゲット: iOS 18+（可能な限りiOS 26）

## 禁止API（使ったら即修正）
- `NavigationView` → `NavigationStack` を使え
- `@StateObject` (KMP ViewModel用) → `@StateViewModel` を使え
- `@ObservedObject` (KMP ViewModel用) → `@ObservedViewModel` を使え
- `.onChange(of:) { newValue in }` → `.onChange(of:) { oldValue, newValue in }` を使え

## ViewModelWrapper禁止
- ObservableObject Wrapperクラスを作るな
- KMP-ObservableViewModelでKotlin VMを直接使え
- `import KMPObservableViewModelSwiftUI` を使え
- Timer.publishによるポーリング監視は禁止

## ナビゲーション
- `NavigationStack` を使え（`NavigationView` は非推奨）
- `NavigationLink(destination:)` より `NavigationLink(value:)` + `.navigationDestination` を推奨

## ドキュメントコメント
- **struct/class宣言**には日本語でドキュメントコメントを記載すること
  - 基本形式: `/// 説明` （トリプルスラッシュ）
  - 複数段落が必要な場合: 空行で区切る
  - 注意事項がある場合: `- Note:` や `- Warning:` を使用
- **Viewのプロパティ**には日本語でドキュメントコメントを記載すること
  - 基本形式: `/// ノード一覧` （トリプルスラッシュ）
  - let, @Binding, @State, @StateViewModel等の全プロパティに記載

**例**:
```swift
/// ノード詳細画面
///
/// ノードの詳細情報を表示し、編集・削除・派生投稿などの操作を提供する。
///
/// - Note: 編集・削除機能はオーナーのみ使用可能
struct DetailView: View {
    /// 表示するノードのID
    let nodeId: String
    /// 詳細画面のViewModel
    @StateViewModel var viewModel = KoinHelper().getDetailViewModel()
```

**参考**: 詳細な記述が必要な場合は[Swift公式ドキュメントコメント規約](https://github.com/swiftlang/swift/blob/main/docs/DocumentationComments.md)を参照

## コードレビュー時のチェックリスト
1. NavigationView が使われていないか
2. ViewModelWrapper/Timer.publish が使われていないか
3. @StateObject が KMP ViewModel に使われていないか（@StateViewModel を使うべき）
4. iOS 16以降の非推奨Warning がないか
5. SwiftUI Preview が壊れていないか
6. struct/class宣言とプロパティに日本語ドキュメントコメントが記載されているか
