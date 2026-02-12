# SwiftUI Preview 状況レポート

## 概要

Phase R3で実施したSwiftUI Preview復活作業の結果をまとめます。

## 動作するPreview

以下のコンポーネントPreviewは**動作します**:

### コアコンポーネント
- `NodeCardView` (HomeView.swift)
  - #Preview("NodeCardView")
  - #Preview("NodeCardView - Derived")
- `UserAvatarView` (UserAvatarView.swift)
  - #Preview("UserAvatarView - With Image")
  - #Preview("UserAvatarView - Fallback")

### Detail系コンポーネント
- `DetailHeaderSection` (DetailHeaderSection.swift)
  - #Preview("DetailHeaderSection - Idea")
  - #Preview("DetailHeaderSection - Issue")
- `DetailReactionBar` (DetailReactionBar.swift)
  - #Preview("DetailReactionBar")
  - #Preview("DetailReactionBar - Not Authenticated")
- `DetailCommentsView` (DetailCommentsView.swift)
  - #Preview("DetailCommentsView - With Comments")
  - #Preview("DetailCommentsView - Empty")
- `DetailDerivationTreeView` (DetailDerivationTreeView.swift)
  - #Preview("DerivationTree - With Parent")
  - #Preview("DerivationTree - With Children")
- `DetailEditView` (DetailEditView.swift)
  - #Preview("DetailEditView")
  - #Preview("DetailEditView - Error")

### Main系コンポーネント
- `PostTypeSelectSheet` (PostTypeSelectSheet.swift, MainTabView.swift)
  - #Preview("PostTypeSelectSheet")

## 動作しないPreview（コメントアウト済み）

以下の画面全体のPreviewはKoinHelper依存のため**現状では動作しません**:

### Screen Views
- `HomeView` (HomeView.swift)
- `DetailView` (DetailView.swift)
- `DiscoverView` (DiscoverView.swift)
- `MapView` (MapView.swift)
- `MyPageView` (MyPageView.swift)
- `RootView` (RootView.swift)
- `LoginView` (LoginView.swift)
- `MainTabView` (MainTabView.swift)

### Form Views
- `IssuePostView` (IssuePostView.swift)
- `IdeaPostView` (IdeaPostView.swift)
- `DerivedPostView` (DerivedPostView.swift)

## Preview用共通データ

`Utilities/PreviewData.swift` を作成し、Preview用のサンプルデータを集約しました:

- `PreviewData.sampleNode`
- `PreviewData.sampleIssueNode`
- `PreviewData.sampleDerivedNode`
- `PreviewData.sampleNodes`
- `PreviewData.sampleComment`
- `PreviewData.sampleCommentWithReplies`
- `PreviewData.sampleComments`
- `PreviewData.sampleUser`
- `PreviewData.sampleTag`
- `PreviewData.sampleTags`

## 今後の改善方針

### Phase R5以降で実施予定

1. **Preview用Mock ViewModel作成**
   - KoinHelper依存を解消するため、PreviewでMock ViewModelを注入できる仕組みを構築
   - Kotlin側でPreview用のFake実装を提供

2. **SPMマルチモジュール化対応**
   - Issue #38で計画されているSPMマルチモジュール化に合わせてPreview環境を整備
   - Previewモジュールを分離してMock依存を明確化

3. **Previewの自動テスト化**
   - Previewが正常にレンダリングできることを自動検証するテストを追加
   - CIでPreviewビルドエラーを検知

## 使い方

### 動作するPreviewを確認する方法

1. Xcodeでプロジェクトを開く:
   ```bash
   open iosApp/iosApp.xcodeproj
   ```

2. 以下のファイルを開いてPreviewを表示:
   - `Views/Screens/Home/HomeView.swift` → NodeCardViewのPreview
   - `Views/Screens/Detail/DetailHeaderSection.swift` → DetailHeaderSectionのPreview
   - `Views/Components/Common/UserAvatarView.swift` → UserAvatarViewのPreview

3. Xcode右側のPreviewパネルで確認（Command + Option + Enter）

### コメントアウトされたPreviewを有効化する方法（将来）

Mock ViewModelを作成後、各ファイルのPreviewセクションで以下を実施:

1. コメントアウトを解除
2. Mock ViewModelをインジェクト:
   ```swift
   #Preview("HomeView") {
       NavigationStack {
           HomeView(viewModel: PreviewMockHomeViewModel())
       }
   }
   ```

## 関連Issue

- Issue #37: Add Previewが可能なようにPreview用のモジュールを作る
- Issue #38: iosAppをSPMでマルチモジュール化する（Phase R5）
