# SPM マルチモジュール アーキテクチャ設計書

## 1. 背景と課題

### 現状の問題

現在のiOS View層は、全てのViewが `ViewModelWrapper`（KMP Shared依存）に直接依存している。

```
HomeView → HomeViewModelWrapper → HomeViewModel (KMP)
                                    ↓
                              import Shared（KMPフレームワーク）
```

**これにより以下の問題が発生:**

1. **SwiftUI Preview不可**: Viewが `@StateObject var viewModel = HomeViewModelWrapper()` を持ち、`init()` で `KoinHelper()` を呼ぶため、Preview時にKMPフレームワーク全体の初期化が必要
2. **UIの単体テスト不可**: View単体を切り出してスナップショットテスト等ができない
3. **ビルド時間**: Viewの微修正でもKMPフレームワーク依存のリビルドが走る
4. **関心の混在**: UIの見た目ロジック（色、レイアウト）とデータバインディングが同一ファイルに同居

### 具体例: 現状の `NodeCardView`

```swift
// 現状: KMP の Node 型に直接依存
struct NodeCardView: View {
    let node: Node  // ← import Shared 必須

    var body: some View {
        // node.type == .issue という比較で NodeType (KMP enum) に依存
        // formatDate(node.createdAt) で Kotlinx_datetimeInstant に依存
    }
}
```

## 2. 設計方針

### コアコンセプト

> **ViewはString/Int/Bool等の基本型のみをInputに持つ純粋な表示関数である。**

```
UIモジュール (InspireHubUI)          Screen層 (iosApp)
┌─────────────────────┐        ┌─────────────────────┐
│ NodeCardView(        │   ←──  │ HomeScreen が         │
│   title: String,     │        │ ViewModelWrapper から  │
│   body: String,      │        │ 基本型に変換して注入    │
│   typeLabel: String,  │        └─────────────────────┘
│   ...                │                  ↓
│ )                    │        ┌─────────────────────┐
│ ※ import Shared なし  │        │ ViewModelWrapper      │
│ ※ Preview可能        │        │ (KMP依存はここだけ)    │
└─────────────────────┘        └─────────────────────┘
```

### レイヤー定義

| レイヤー | 場所 | 責務 | KMP依存 |
|----------|------|------|---------|
| **UIモジュール** | `iosApp/InspireHubUI/` (SPM) | 純粋な表示。基本型のみInput | なし |
| **Screen層** | `iosApp/iosApp/Screens/` | ViewModelWrapper → UIモジュールへのデータ注入 | あり |
| **ViewModelWrapper層** | `iosApp/iosApp/ViewModelWrappers/` (既存) | KMP StateFlow → @Published 変換 | あり |

## 3. ディレクトリ構成

### 全体構成

```
iosApp/
├── InspireHubUI/                    # ★ 新規: SPM Package
│   ├── Package.swift
│   └── Sources/
│       └── InspireHubUI/
│           ├── Components/          # 再利用可能なUIパーツ
│           │   ├── NodeCardView.swift
│           │   ├── NodeTypeBadge.swift
│           │   ├── CommentRow.swift
│           │   ├── TagChip.swift
│           │   ├── FlowLayout.swift
│           │   ├── ErrorStateView.swift
│           │   ├── LoadingStateView.swift
│           │   └── NodeRowView.swift
│           └── Views/               # 画面単位のView
│               ├── LoginContentView.swift
│               ├── HomeFeedView.swift
│               ├── NodeDetailContentView.swift
│               ├── MapListView.swift
│               ├── MyPageContentView.swift
│               ├── PostFormView.swift
│               └── PostTypeSelectView.swift
│
├── iosApp/                          # 既存アプリ（Screen層に再構成）
│   ├── iOSApp.swift                 # エントリーポイント（変更なし）
│   ├── ViewModelWrappers/           # 既存（変更なし）
│   │   ├── HomeViewModelWrapper.swift
│   │   ├── DetailViewModelWrapper.swift
│   │   ├── MapViewModelWrapper.swift
│   │   ├── MyPageViewModelWrapper.swift
│   │   └── PostViewModelWrapper.swift
│   ├── Screens/                     # ★ 新規: 接続層
│   │   ├── RootScreen.swift
│   │   ├── LoginScreen.swift
│   │   ├── MainTabScreen.swift
│   │   ├── HomeScreen.swift
│   │   ├── DetailScreen.swift
│   │   ├── MapScreen.swift
│   │   ├── MyPageScreen.swift
│   │   ├── IssuePostScreen.swift
│   │   ├── IdeaPostScreen.swift
│   │   └── DerivedPostScreen.swift
│   └── (旧View: 移行完了後に削除)
│
└── iosApp.xcodeproj/
```

## 4. SPM Package 設計

### Package.swift

```swift
// iosApp/InspireHubUI/Package.swift
// swift-tools-version: 5.9

import PackageDescription

let package = Package(
    name: "InspireHubUI",
    platforms: [
        .iOS(.v16)
    ],
    products: [
        .library(
            name: "InspireHubUI",
            targets: ["InspireHubUI"]
        ),
    ],
    targets: [
        .target(
            name: "InspireHubUI",
            dependencies: [],
            path: "Sources/InspireHubUI"
        ),
    ]
)
```

**設計判断:**
- 依存ライブラリ: **ゼロ**（純粋SwiftUI + Foundation のみ）
- iOS 16+ 対応（既存プロジェクトと合わせる）
- ターゲットは1つ（`InspireHubUI`）。画面数が少ないため分割不要

### Xcodeプロジェクトへの統合

1. Xcode で `iosApp.xcodeproj` を開く
2. File → Add Package Dependencies → Add Local → `iosApp/InspireHubUI` を選択
3. iosApp ターゲットに `InspireHubUI` を Link

## 5. 各画面の View Input 定義一覧

### 5.1 Components（再利用パーツ）

#### NodeCardView

```swift
struct NodeCardView: View {
    let title: String
    let body: String
    let typeLabel: String        // "課題" | "アイデア"
    let typeIconName: String     // "exclamationmark.triangle.fill" | "lightbulb.fill"
    let isIssue: Bool            // バッジカラー分岐用
    let createdAtText: String    // "3時間前" 等（フォーマット済み文字列）
}
```

#### NodeTypeBadge

```swift
struct NodeTypeBadge: View {
    let label: String            // "課題" | "アイデア"
    let iconName: String         // SF Symbols名
    let isIssue: Bool            // カラー分岐用
}
```

#### CommentRow

```swift
struct CommentRow: View {
    let authorId: String
    let content: String
}
```

#### TagChip

```swift
struct TagChip: View {
    let text: String
}
```

#### ErrorStateView

```swift
struct ErrorStateView: View {
    let message: String
    let onRetry: () -> Void
}
```

#### LoadingStateView

```swift
struct LoadingStateView: View {
    let message: String          // "読み込み中..." 等
}
```

#### NodeRowView

```swift
struct NodeRowView: View {
    let title: String
    let content: String
    let typeIconName: String
    let isIssue: Bool
    let isIndented: Bool         // 派生ノードのインデント
}
```

### 5.2 Views（画面単位）

#### LoginContentView

```swift
struct LoginContentView: View {
    let isLoading: Bool
    let error: String?
    let onGoogleSignIn: () -> Void
}
```

#### HomeFeedView

```swift
struct HomeFeedView: View {
    // タブ
    let tabs: [String]             // ["新着", "課題", "アイデア", "自分"]
    let selectedTabIndex: Int
    let onTabSelected: (Int) -> Void

    // ソート
    let sortOptions: [String]      // ["新しい順", "人気順"]
    let selectedSortIndex: Int
    let onSortSelected: (Int) -> Void

    // ノードリスト
    let nodes: [NodeCardInput]
    let onNodeTap: (String) -> Void  // nodeId

    // 状態
    let isLoading: Bool
    let error: String?
    let onRetry: () -> Void
    let onRefresh: () -> Void
}

/// ノードカード用Input構造体
struct NodeCardInput: Identifiable {
    let id: String
    let title: String
    let body: String
    let typeLabel: String
    let typeIconName: String
    let isIssue: Bool
    let createdAtText: String
}
```

#### NodeDetailContentView

```swift
struct NodeDetailContentView: View {
    // ノード情報
    let title: String
    let content: String
    let typeLabel: String
    let typeIconName: String
    let isIssue: Bool
    let authorId: String
    let parentNodeId: String?

    // リアクション
    let onLikeTap: () -> Void
    let onDerivePost: () -> Void

    // 親ノード
    let onParentNodeTap: ((String) -> Void)?

    // 子ノード
    let childNodes: [ChildNodeInput]
    let onChildNodeTap: (String) -> Void

    // コメント
    let comments: [CommentInput]
    let commentText: String
    let isCommentSubmitting: Bool
    let onCommentTextChanged: (String) -> Void
    let onCommentSubmit: () -> Void
}

struct ChildNodeInput: Identifiable {
    let id: String
    let title: String
}

struct CommentInput: Identifiable {
    let id: String
    let authorId: String
    let content: String
}
```

#### MapListView

```swift
struct MapListView: View {
    let nodes: [NodeRowInput]
    let isLoading: Bool
    let error: String?
    let onNodeTap: (String) -> Void
    let onRetry: () -> Void
    let onRefresh: () -> Void
}

struct NodeRowInput: Identifiable {
    let id: String
    let title: String
    let content: String
    let typeIconName: String
    let isIssue: Bool
    let isChild: Bool
}
```

#### MyPageContentView

```swift
struct MyPageContentView: View {
    // プロフィール
    let userName: String?
    let roleTag: String?

    // 自分の投稿
    let myNodes: [NodeRowInput]
    let isLoading: Bool

    let onNodeTap: (String) -> Void
}
```

#### PostFormView（課題/アイデア/派生を統合）

```swift
struct PostFormView: View {
    let navigationTitle: String     // "課題を投稿" | "アイデアを投稿" | "派生アイデアを投稿"
    let titlePlaceholder: String    // "課題のタイトルを入力" 等

    // 派生元情報（派生投稿時のみ）
    let parentNodeTitle: String?
    let parentNodeBody: String?
    let parentNodeTypeLabel: String?
    let parentNodeIsIssue: Bool?

    // フォーム状態
    let title: String
    let content: String
    let tags: [String]

    // コールバック
    let onTitleChanged: (String) -> Void
    let onContentChanged: (String) -> Void
    let onAddTag: (String) -> Void
    let onSubmit: () -> Void
    let onCancel: () -> Void

    // 送信状態
    let isSubmitting: Bool
    let error: String?
}
```

#### PostTypeSelectView

```swift
struct PostTypeSelectView: View {
    let onIssueSelected: () -> Void
    let onIdeaSelected: () -> Void
    let onDismiss: () -> Void
}
```

## 6. Screen層の実装例

Screen層はViewModelWrapperからUIモジュールへのデータ変換と注入を担当する。

### HomeScreen.swift の例

```swift
// iosApp/iosApp/Screens/HomeScreen.swift
import SwiftUI
import Shared
import InspireHubUI

struct HomeScreen: View {
    @StateObject var viewModel = HomeViewModelWrapper()

    var body: some View {
        NavigationView {
            HomeFeedView(
                tabs: HomeTabUI.allCases.map(\.rawValue),
                selectedTabIndex: HomeTabUI.allCases.firstIndex(of: viewModel.currentTab) ?? 0,
                onTabSelected: { index in
                    viewModel.setTab(HomeTabUI.allCases[index])
                },
                sortOptions: SortOrderUI.allCases.map(\.rawValue),
                selectedSortIndex: SortOrderUI.allCases.firstIndex(of: viewModel.sortOrder) ?? 0,
                onSortSelected: { index in
                    viewModel.setSortOrder(SortOrderUI.allCases[index])
                },
                nodes: viewModel.nodes.map { node in
                    NodeCardInput(
                        id: node.id,
                        title: node.title,
                        body: node.content,
                        typeLabel: node.type == .issue ? "課題" : "アイデア",
                        typeIconName: node.type == .issue
                            ? "exclamationmark.triangle.fill"
                            : "lightbulb.fill",
                        isIssue: node.type == .issue,
                        createdAtText: formatDate(node.createdAt)
                    )
                },
                onNodeTap: { nodeId in
                    // NavigationLink等で遷移
                },
                isLoading: viewModel.isLoading && viewModel.nodes.isEmpty,
                error: viewModel.nodes.isEmpty ? viewModel.error : nil,
                onRetry: { viewModel.loadNodes() },
                onRefresh: { viewModel.refresh() }
            )
            .navigationTitle("InspireHub")
            .navigationBarTitleDisplayMode(.inline)
            .onAppear { viewModel.loadNodes() }
        }
    }

    private func formatDate(_ instant: Kotlinx_datetimeInstant) -> String {
        let date = Date(timeIntervalSince1970: TimeInterval(instant.epochSeconds))
        let formatter = RelativeDateTimeFormatter()
        formatter.locale = Locale(identifier: "ja_JP")
        formatter.unitsStyle = .short
        return formatter.localizedString(for: date, relativeTo: Date())
    }
}
```

### DetailScreen.swift の例

```swift
// iosApp/iosApp/Screens/DetailScreen.swift
import SwiftUI
import Shared
import InspireHubUI

struct DetailScreen: View {
    let nodeId: String
    @StateObject private var viewModel = DetailViewModelWrapper()

    var body: some View {
        Group {
            if let node = viewModel.selectedNode {
                NodeDetailContentView(
                    title: node.title,
                    content: node.content,
                    typeLabel: node.type == .issue ? "課題" : "アイデア",
                    typeIconName: node.type == .issue
                        ? "exclamationmark.circle.fill" : "lightbulb.fill",
                    isIssue: node.type == .issue,
                    authorId: node.authorId,
                    parentNodeId: node.parentNodeId,
                    onLikeTap: { viewModel.toggleLike() },
                    onDerivePost: { /* 派生投稿画面遷移 */ },
                    onParentNodeTap: node.parentNodeId != nil
                        ? { id in /* NavigationLink */ } : nil,
                    childNodes: viewModel.childNodes.map {
                        ChildNodeInput(id: $0.id, title: $0.title)
                    },
                    onChildNodeTap: { id in /* NavigationLink */ },
                    comments: viewModel.comments.map {
                        CommentInput(id: $0.id, authorId: $0.authorId, content: $0.content)
                    },
                    commentText: viewModel.commentText,
                    isCommentSubmitting: viewModel.isCommentSubmitting,
                    onCommentTextChanged: { viewModel.updateCommentText($0) },
                    onCommentSubmit: { viewModel.submitComment() }
                )
            } else if let error = viewModel.error {
                ErrorStateView(message: error) {
                    viewModel.loadDetail(nodeId: nodeId)
                }
            } else {
                LoadingStateView(message: "読み込み中...")
            }
        }
        .navigationBarTitleDisplayMode(.inline)
        .onAppear { viewModel.loadDetail(nodeId: nodeId) }
    }
}
```

## 7. 移行計画

### 移行原則

1. **既存コードを壊さない**: 旧Viewと新Screen/UIモジュールが並存する期間を設ける
2. **ボトムアップ移行**: 末端のコンポーネントから移行し、最後にScreen層で差し替え
3. **1画面ずつ検証**: 各画面の移行完了後にPreview動作確認してから次へ

### Phase 1: 基盤構築

| Step | 作業内容 |
|------|----------|
| 1-1 | `iosApp/InspireHubUI/` に Package.swift を作成 |
| 1-2 | Xcodeプロジェクトに Local Package として追加 |
| 1-3 | 共通コンポーネント移行: `TagChip`, `FlowLayout`, `ErrorStateView`, `LoadingStateView` |
| 1-4 | Input用の構造体定義: `NodeCardInput`, `CommentInput`, `ChildNodeInput`, `NodeRowInput` |

### Phase 2: コンポーネント移行（依存なし画面から）

| Step | 対象 | 理由 |
|------|------|------|
| 2-1 | `PostTypeSelectView` | 最もシンプル。コールバック2つのみ。移行手順の検証に最適 |
| 2-2 | `NodeCardView` → `NodeCardView` + `NodeTypeBadge` | 他画面で再利用される中核パーツ |
| 2-3 | `CommentRow` | DetailViewで使う末端パーツ |
| 2-4 | `NodeRowView` | Map/MyPageで使う末端パーツ |

### Phase 3: 画面View移行

| Step | 対象 | 複雑度 | 備考 |
|------|------|--------|------|
| 3-1 | `LoginContentView` + `LoginScreen` | 低 | Input少。認証フロー（Google Sign-In）はScreen層に残る |
| 3-2 | `PostFormView` + `IssuePostScreen` / `IdeaPostScreen` / `DerivedPostScreen` | 中 | 3画面を1つのPostFormViewに統合。移行効果大 |
| 3-3 | `MyPageContentView` + `MyPageScreen` | 低 | プレースホルダ的画面。Input少ない |
| 3-4 | `MapListView` + `MapScreen` | 低 | ノードリスト表示のみ |
| 3-5 | `HomeFeedView` + `HomeScreen` | 高 | タブ・ソート・リスト・ナビゲーション。最も複雑 |
| 3-6 | `NodeDetailContentView` + `DetailScreen` | 高 | コメント入力・リアクション・子ノード。最も多機能 |

### Phase 4: クリーンアップ

| Step | 作業内容 |
|------|----------|
| 4-1 | 旧Viewファイル削除（`HomeView.swift`, `DetailView.swift` 等） |
| 4-2 | `RootView.swift` → `RootScreen.swift` に移行 |
| 4-3 | `MainTabView.swift` → `MainTabScreen.swift` に移行 |
| 4-4 | `ContentView.swift` 削除（未使用レガシー） |

### 移行順序の根拠

```
PostTypeSelectView（最小）
    ↓
NodeCardView等のパーツ（他画面の前提）
    ↓
LoginContentView（小さい画面で手順確認）
    ↓
PostFormView（3画面統合で大きな効果）
    ↓
MyPage / Map（小さい画面を片付ける）
    ↓
Home / Detail（最も複雑な画面を最後に）
```

## 8. Preview可能性の確認

### UIモジュール内のPreview例

```swift
// InspireHubUI/Sources/InspireHubUI/Components/NodeCardView.swift
struct NodeCardView_Previews: PreviewProvider {
    static var previews: some View {
        VStack(spacing: 12) {
            NodeCardView(
                title: "社内の情報共有が非効率",
                body: "Slackのチャンネルが多すぎて、必要な情報にたどり着けない...",
                typeLabel: "課題",
                typeIconName: "exclamationmark.triangle.fill",
                isIssue: true,
                createdAtText: "3時間前"
            )
            NodeCardView(
                title: "AIチャットボットで社内FAQ自動応答",
                body: "よくある質問をAIが自動で回答するシステムを作りたい",
                typeLabel: "アイデア",
                typeIconName: "lightbulb.fill",
                isIssue: false,
                createdAtText: "1日前"
            )
        }
        .padding()
        // ← import Shared なし。KMP不要。即座にプレビュー可能
    }
}
```

## 9. 設計判断の補足

### なぜSPM Packageを1つに留めるか

画面数が11、コンポーネント数が8程度の規模では、パッケージを分割するメリット（ビルド時間短縮、依存の明確化）よりも管理コストの方が大きい。将来的にデザインシステムが確立されたら `InspireHubDesignSystem` への分離を検討する。

### なぜColor型をInputに含めないか

`Color` はSwiftUIフレームワーク依存であり基本型ではないが、UIモジュール内での使用は許容する。ただし、**外からColorを注入するのではなく、`isIssue: Bool` のようなセマンティックなフラグで渡し、UIモジュール内部でカラーを決定する**。これによりデザインの一貫性を保つ。

### PostFormViewの統合について

現状、`IssuePostView` / `IdeaPostView` / `DerivedPostView` は95%同一のコード。差分は:
- ナビゲーションタイトル
- プレースホルダテキスト
- 派生元ノード表示（DerivedPostViewのみ）
- 送信メソッド（submitIssue / submitIdea / submitDerived）

これらは全てInputパラメータで制御可能なため、`PostFormView` 1つに統合する。送信メソッドの分岐はScreen層で行う。
