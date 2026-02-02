# InspireHub Mobile iOS - Phase1 QAチェックリスト

> **作成日**: 2026-02-02
> **検証方法**: コードレベル全数検証（A: UIコンポーネント / B: Wrapper呼び出し / C: Kotlin VM呼び出し / D: ロジック実装）

## 検証結果サマリ

| 重要度 | 件数 | 概要 |
|--------|------|------|
| 🔴 致命的 | 3件 | コンパイルエラー、画面未接続 |
| 🟡 重要 | 5件 | 機能未実装、設計との乖離 |
| 🔵 軽微 | 4件 | UI不足、改善推奨 |

---

## 1. 認証画面（RootView / LoginView）

### 1-1. Googleログイン
| 段階 | 確認項目 | 結果 |
|------|----------|------|
| A | LoginView: Google Sign-In ボタン | ✅ Button `handleGoogleSignIn` |
| B | AuthViewModelWrapper.verifyGoogleToken() | ✅ L69 |
| C | AuthViewModel.verifyGoogleToken() | ✅ L111 |
| D | authRepository.verifyGoogleToken() 呼び出し | ✅ |

### 1-2. ログアウト
| 段階 | 確認項目 | 結果 |
|------|----------|------|
| A | HomeView: ログアウトボタン | ❌ **HomeView にログアウトボタンなし** |
| B | AuthViewModelWrapper.logout() | ✅ L61 |
| C | AuthViewModel.logout() | ✅ L87 |
| D | authRepository.logout() 呼び出し | ✅ |

> ⚠️ 旧HomeView.swiftにはログアウトボタンがあったが、現HomeViewはノード一覧表示に変更済み。ログアウト導線が消失。

### 1-3. モックログイン（DEBUG用）
| 段階 | 確認項目 | 結果 |
|------|----------|------|
| A | LoginView: モックログインボタン | ❓ 未確認（LoginView内に明示ボタンなし、別導線の可能性） |
| B | AuthViewModelWrapper.mockLogin() | ✅ L69 |
| C | AuthViewModel.mockLogin() | ✅ L112 |
| D | UserStore.login() 直接呼び出し | ✅ |

### 1-4. 認証状態による画面切替
| 段階 | 確認項目 | 結果 |
|------|----------|------|
| A | RootView: isAuthenticated による分岐 | ✅ L10-11 |
| B | AuthViewModelWrapper.isAuthenticated | ✅ @Published |
| C | AuthViewModel.isAuthenticated (StateFlow) | ✅ UserStore経由 |
| D | UserStore.isAuthenticated 監視 | ✅ |

---

## 2. メインタブ（MainTabView）

### 2-1. タブ切替（ホーム/マップ/マイページ）
| 段階 | 確認項目 | 結果 |
|------|----------|------|
| A | TabView + 3タブ | ✅ tag 0/1/2 |

### 🔴 BUG-001: マップタブがプレースホルダのまま【致命的】
**ファイル**: `MainTabView.swift:24`
**現状**: `MapPlaceholderView()` を使用（「マップ機能は今後実装予定」表示）
**あるべき姿**: `MapView()` を使用（MapView.swift は実装済み）
**影響**: マップ画面が全く表示されない

### 🔴 BUG-002: マイページタブがプレースホルダのまま【致命的】
**ファイル**: `MainTabView.swift:31`
**現状**: `MyPagePlaceholderView()` を使用（「マイページは今後実装予定」表示）
**あるべき姿**: `MyPageView()` を使用（MyPageView.swift は実装済み）
**影響**: マイページ画面が全く表示されない

### 2-2. FAB（投稿ボタン）
| 段階 | 確認項目 | 結果 |
|------|----------|------|
| A | FAB Button (plus アイコン) | ✅ L40-51 |
| A | sheet → PostTypeSelectSheet | ✅ L55-71 |
| A | fullScreenCover → IssuePostView | ✅ L72-74 |
| A | fullScreenCover → IdeaPostView | ✅ L75-77 |

---

## 3. ホーム画面（HomeView）

### 3-1. タブ切替（新着/課題/アイデア/自分）
| 段階 | 確認項目 | 結果 |
|------|----------|------|
| A | HomeTabUI 4タブ + ボタン | ✅ L45-61 |
| B | HomeViewModelWrapper.setTab() | ✅ L103 |
| C | HomeViewModel.setTab() | ✅ L51 |
| D | nodeStore.setTab() + loadNodes() | ✅ |

### 3-2. ソート切替（新着順/人気順）
| 段階 | 確認項目 | 結果 |
|------|----------|------|
| A | Menu + SortOrderUI | ✅ L72-89 |
| B | HomeViewModelWrapper.setSortOrder() | ✅ L108 |
| C | HomeViewModel.setSortOrder() | ✅ L57 |
| D | nodeStore.setSortOrder() | ✅ |

### 3-3. ノードタップ → 詳細遷移
| 段階 | 確認項目 | 結果 |
|------|----------|------|
| A | NavigationLink → DetailView(nodeId:) | ✅ L98 |

### 3-4. プルリフレッシュ
| 段階 | 確認項目 | 結果 |
|------|----------|------|
| A | .refreshable | ✅ L107 |
| B | HomeViewModelWrapper.refresh() | ✅ L99 |
| C | HomeViewModel.refresh() | ✅ L49 |
| D | loadNodes(forceRefresh: true) | ✅ |

### 3-5. いいね（一覧から）
| 段階 | 確認項目 | 結果 |
|------|----------|------|
| A | NodeCardView にいいねボタン | ❌ **カード上にリアクションボタンなし** |
| B | HomeViewModelWrapper.toggleLike(nodeId:) | ✅ L113（実装済みだがView未使用） |
| C | HomeViewModel.toggleLike(nodeId:) | ✅ L60 |
| D | nodeRepository.toggleLike() | ✅ |

> 🔵 設計書では一覧からのリアクション操作は「△」（簡易版/余裕あれば）なので軽微。

### 3-6. リアクション数・コメント数表示
| 段階 | 確認項目 | 結果 |
|------|----------|------|
| A | NodeCardView にカウント表示 | ❌ **表示なし** |
| D | Node ドメインモデルに likeCount/commentCount フィールド | ❌ **フィールド未定義** |

> 🟡 設計書のカード構成「👍 12 💡 8 💬 3 🤝 2」が実装されていない。Node ドメインモデルに該当フィールドがない（NodeDto にはある）。

---

## 4. マップ画面（MapView）

### 4-1. ノード一覧表示
| 段階 | 確認項目 | 結果 |
|------|----------|------|
| A | LazyVStack + ForEach(nodes) | ✅ L50-56 |
| B | MapViewModelWrapper.loadNodes() | ✅ L43 |
| C | MapViewModel.loadNodes() | ✅ L25 |
| D | nodeRepository.getNodes() | ✅ |

### 4-2. ノードタップ → 詳細遷移
| 段階 | 確認項目 | 結果 |
|------|----------|------|
| A | NavigationLink → DetailView(nodeId:) | ✅ L52 |

### 4-3. プルリフレッシュ
| 段階 | 確認項目 | 結果 |
|------|----------|------|
| A | .refreshable | ✅ L59 |
| B | MapViewModelWrapper.loadNodes() | ✅ |

### 4-4. ツリー表示（派生関係可視化）
| 段階 | 確認項目 | 結果 |
|------|----------|------|
| A | parentNodeId による indent | ✅ L51, L67-71（簡易インデント） |
| B | MapViewModelWrapper.getNodeTree() | ✅（実装済み） |
| C | MapViewModel.getNodeTree() | ✅（実装済み） |

> 🔵 getNodeTree() は実装済みだが MapView は使用していない。フラットリスト+インデントで代替中。Phase1としては許容範囲。

---

## 5. マイページ画面（MyPageView）

### 5-1. プロフィール表示
| 段階 | 確認項目 | 結果 |
|------|----------|------|
| A | ユーザー名・roleTag 表示 | ✅ L30-42 |
| B | MyPageViewModelWrapper.currentUser | ✅ @Published |
| C | MyPageViewModel.currentUser | ✅ UserStore.currentUser |

### 5-2. 自分の投稿一覧
| 段階 | 確認項目 | 結果 |
|------|----------|------|
| A | LazyVStack + ForEach(myNodes) | ✅ L82-89 |
| B | MyPageViewModelWrapper.loadMyNodes() | ✅ L32 |
| C | MyPageViewModel.loadMyNodes() | ✅ L28 |
| D | nodeRepository.getNodes() + authorId filter | ✅ |

### 5-3. ノードタップ → 詳細遷移
| 段階 | 確認項目 | 結果 |
|------|----------|------|
| A | NavigationLink → DetailView(nodeId:) | ✅ L84 |

### 🟡 BUG-003: 「リアクションした投稿」セクション未実装【重要】
**ファイル**: `MyPageView.swift`
**設計書**: マイページ → 「自分の投稿一覧、リアクションした投稿一覧」
**現状**: 「自分の投稿」セクションのみ
**不足**: 「リアクションした投稿」セクションが存在しない
**Kotlin側**: MyPageViewModel にも該当メソッドなし

---

## 6. 詳細画面（DetailView）

### 6-1. ノード詳細表示（タイトル/本文/投稿者/タイプ）
| 段階 | 確認項目 | 結果 |
|------|----------|------|
| A | headerSection + bodySection + metaSection | ✅ |
| B | DetailViewModelWrapper.selectedNode | ✅ @Published |
| C | DetailViewModel.selectedNode | ✅ nodeStore.selectedNode |
| D | nodeRepository.getNode() → nodeStore.selectNode() | ✅ |

### 6-2. 派生元ノード表示・遷移
| 段階 | 確認項目 | 結果 |
|------|----------|------|
| A | parentSection: NavigationLink → DetailView(nodeId:) | ✅ L102-116 |
| A | parentNodeId 条件分岐 | ✅ L42-44 |

### 6-3. いいねリアクション
| 段階 | 確認項目 | 結果 |
|------|----------|------|
| A | reactionBar: いいねボタン | ✅ L122 |
| B | DetailViewModelWrapper.toggleLike() | ✅ L70 |
| C | DetailViewModel.toggleLike() | ✅ L77 |
| D | nodeRepository.toggleLike() → nodeStore.selectNode() | ✅ |

### 🟡 BUG-004: 共感/気になる/作りたいリアクション未実装【重要】
**ファイル**: `DetailView.swift:125-127`
**現状**: `reactionButton(emoji: "💡", label: "共感") { }` — アクション空
**影響**: 共感/気になる/作りたいの3リアクションがタップしても何も起きない
**Kotlin側**: DetailViewModel にも対応メソッドなし。Node モデルにも対応フィールドなし。

### 6-4. コメント表示
| 段階 | 確認項目 | 結果 |
|------|----------|------|
| A | ForEach(comments) + commentRow | ✅ L222-229 |
| B | DetailViewModelWrapper.comments | ✅ @Published |
| C | DetailViewModel.comments | ✅ MutableStateFlow |
| D | commentRepository.getComments() | ✅ |

### 6-5. コメント投稿
| 段階 | 確認項目 | 結果 |
|------|----------|------|
| A | TextField + submitComment ボタン | ✅ L206-218 |
| B | DetailViewModelWrapper.updateCommentText() / submitComment() | ✅ L74, L78 |
| C | DetailViewModel.updateCommentText() / submitComment() | ✅ L89, L96 |
| D | commentRepository.createComment() | ✅ |

### 6-6. 派生ノード（子ノード）一覧
| 段階 | 確認項目 | 結果 |
|------|----------|------|
| A | ForEach(childNodes) + NavigationLink | ✅ L173-192 |
| B | DetailViewModelWrapper.childNodes | ✅ @Published |
| C | DetailViewModel.childNodes | ✅ MutableStateFlow |
| D | nodeRepository.getChildNodes() | ✅ |

### 🔴 BUG-005: 派生投稿ボタンがコンパイルエラー【致命的】
**ファイル**: `DetailView.swift`
**現状**:
- L48: `deriveButton(node: node)` ← **関数呼び出し（引数あり）**
- L147: `private var deriveButton: some View` ← **computed property（引数なし）**
- `deriveButton` は computed property として定義されているが、`(node: node)` 引数付きで呼ばれている
**影響**: **コンパイルエラー** — ビルドが通らない
**追加問題**:
- L7: `@State private var showDerivedPost = false` が宣言されているが未使用
- L148-150: ボタンアクションが `// Phase 2: 派生投稿画面遷移` コメントのみで空
- DerivedPostView.swift は実装済みだが、DetailView から遷移する導線がない

### 🟡 BUG-006: 派生投稿への遷移が未実装【重要】
**ファイル**: `DetailView.swift:148-150`
**設計書**: 詳細画面 → 「派生アイデアを投稿」ボタン → 派生投稿画面（モーダル）
**現状**: ボタンUIは存在するがアクションが空
**DerivedPostView.swift**: 実装済み（parentNode受取、フォーム、submitDerived）
**不足**: `.sheet(isPresented: $showDerivedPost) { DerivedPostView(parentNode: node) }` 相当の接続

---

## 7. 投稿種別選択（PostTypeSelectSheet）

### 7-1. 課題投稿選択
| 段階 | 確認項目 | 結果 |
|------|----------|------|
| A | 「課題を投稿」ボタン | ✅ L17-41 |
| A | onIssueSelected コールバック | ✅ |
| A | .presentationDetents([.medium]) | ✅ L80 |

### 7-2. アイデア投稿選択
| 段階 | 確認項目 | 結果 |
|------|----------|------|
| A | 「アイデアを投稿」ボタン | ✅ L43-67 |
| A | onIdeaSelected コールバック | ✅ |

---

## 8. 課題投稿画面（IssuePostView）

### 8-1. タイトル入力
| 段階 | 確認項目 | 結果 |
|------|----------|------|
| A | TextField | ✅ L13-16 |
| B | PostViewModelWrapper.updateTitle() | ✅ L57 |
| C | PostViewModel.updateTitle() | ✅ L35 |

### 8-2. 本文入力
| 段階 | 確認項目 | 結果 |
|------|----------|------|
| A | TextEditor | ✅ L20-24 |
| B | PostViewModelWrapper.updateContent() | ✅ L61 |
| C | PostViewModel.updateContent() | ✅ L39 |

### 8-3. タグ追加
| 段階 | 確認項目 | 結果 |
|------|----------|------|
| A | TextField + ボタン + FlowLayout | ✅ L27-44 |
| B | PostViewModelWrapper.addTag() | ✅ L65 |
| C | PostViewModel.addTag() | ✅ L43 |

### 🔵 BUG-007: タグ削除UIなし【軽微】
**ファイル**: `IssuePostView.swift`, `IdeaPostView.swift`, `DerivedPostView.swift`
**現状**: TagChip にタップハンドラなし。タグ追加後に削除できない。
**Kotlin側**: PostViewModel.removeTag() 実装済み
**Wrapper側**: PostViewModelWrapper.removeTag() 実装済み
**不足**: TagChip に onTap → removeTag 導線なし

### 8-4. 課題投稿（送信）
| 段階 | 確認項目 | 結果 |
|------|----------|------|
| A | toolbar 「投稿」ボタン | ✅ L63-67 |
| B | PostViewModelWrapper.submitIssue() | ✅ L77 |
| C | PostViewModel.submitIssue() | ✅ L60 |
| D | submit(NodeType.ISSUE) → nodeRepository.createNode() | ✅ |

### 8-5. キャンセル
| 段階 | 確認項目 | 結果 |
|------|----------|------|
| A | toolbar 「キャンセル」ボタン → dismiss | ✅ L58-61 |

### 8-6. 投稿成功時の自動閉じ
| 段階 | 確認項目 | 結果 |
|------|----------|------|
| A | .onChange(of: isSuccess) → dismiss | ✅ L78-82 |

### 8-7. バリデーション
| 段階 | 確認項目 | 結果 |
|------|----------|------|
| A | title 空なら投稿ボタン disabled | ✅ L67 |
| A | isSubmitting 中は disabled | ✅ L67 |
| 🔵 | 本文(content)空でも投稿可能 | ⚠️ バリデーションなし |

---

## 9. アイデア投稿画面（IdeaPostView）

IssuePostView と同構造。submitIdea() を呼ぶ点のみ異なる。

| 段階 | 確認項目 | 結果 |
|------|----------|------|
| B | PostViewModelWrapper.submitIdea() | ✅ L81 |
| C | PostViewModel.submitIdea() | ✅ L67 |
| D | submit(NodeType.IDEA) → nodeRepository.createNode() | ✅ |

---

## 10. 派生投稿画面（DerivedPostView）

### 10-1. 派生元表示
| 段階 | 確認項目 | 結果 |
|------|----------|------|
| A | parentNode のタイトル/本文プレビュー | ✅ L14-31 |

### 10-2. parentNode 設定
| 段階 | 確認項目 | 結果 |
|------|----------|------|
| A | .onAppear { viewModel.setParentNode(parentNode) } | ✅ L99-100 |
| B | PostViewModelWrapper.setParentNode() | ✅ L73 |
| C | PostViewModel.setParentNode() | ✅ L53 |

### 10-3. 派生投稿（送信）
| 段階 | 確認項目 | 結果 |
|------|----------|------|
| A | toolbar 「投稿」ボタン | ✅ L84-88 |
| B | PostViewModelWrapper.submitDerived() | ✅ L85 |
| C | PostViewModel.submitDerived() | ✅ L74 |
| D | submit(NodeType.IDEA, parentNodeId) → nodeRepository.createNode() | ✅ |

---

## 不具合一覧（重要度順）

### 🔴 致命的（3件）

| ID | 画面 | 不具合 | 影響 |
|----|------|--------|------|
| BUG-001 | MainTabView | マップタブが MapPlaceholderView のまま | マップ画面が全く使えない |
| BUG-002 | MainTabView | マイページタブが MyPagePlaceholderView のまま | マイページ画面が全く使えない |
| BUG-005 | DetailView | `deriveButton(node:)` 呼び出しと `var deriveButton` 定義の不一致 | コンパイルエラー |

### 🟡 重要（5件）

| ID | 画面 | 不具合 | 影響 |
|----|------|--------|------|
| BUG-003 | MyPageView | 「リアクションした投稿」セクション未実装 | 設計書との乖離 |
| BUG-004 | DetailView | 共感/気になる/作りたいリアクション空実装 | 3種リアクション機能なし |
| BUG-006 | DetailView | 派生投稿ボタン → DerivedPostView 遷移なし | 派生投稿フローが死亡 |
| BUG-008 | HomeView | ログアウト導線消失 | ログアウトできない |
| BUG-009 | HomeView/NodeCardView | リアクション数/コメント数が表示されない | 設計書のカード構成と乖離。Node モデルに該当フィールドなし |

### 🔵 軽微（4件）

| ID | 画面 | 不具合 | 影響 |
|----|------|--------|------|
| BUG-007 | Post系3画面 | タグ削除UIなし（removeTag は実装済み） | タグを間違えても消せない |
| BUG-010 | Post系3画面 | 本文空でも投稿可能（バリデーションなし） | 空投稿が可能 |
| BUG-011 | MapView | getNodeTree() 未使用（フラットリスト+インデントで代替） | ツリー表示精度が低い |
| BUG-012 | DetailView | showDerivedPost @State 宣言済み未使用 | デッドコード |
