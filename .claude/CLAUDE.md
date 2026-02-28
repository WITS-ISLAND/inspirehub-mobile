# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## プロジェクト概要

**InspireHub Mobile** - 社内ハッカソンの参加障壁を下げるためのKotlin Multiplatformモバイルアプリ。

**目的**:
- アイデアや課題を気軽に投稿できる場を提供
- 「作ってみたい」という軽いコミットメントでチーム形成を支援
- アイデアの派生関係を可視化して「見ているだけで楽しい」体験を作る

**対象ユーザー**: 社内ITエンジニア（主にバックエンド/フロントエンドエンジニア）
**プラットフォーム**: Android、iOS

## プロジェクト構造

標準的なKMPプロジェクト構成で、ビジネスロジックとUIを明確に分離：

### `/shared`
プラットフォーム非依存のビジネスロジック・ドメインモデル
- `commonMain/`: 全プラットフォーム共通コード
- `androidMain/`: Android固有の実装
- `iosMain/`: iOS固有の実装
- パッケージ: `io.github.witsisland.inspirehub`

### `/composeApp`
Compose MultiplatformのUIレイヤー
- `androidMain/`: Android固有のUIとMainActivity
- `projects.shared`に依存してビジネスロジックを利用
- パッケージ: `io.github.witsisland.inspirehub`

### `/iosApp`
iOSアプリのエントリーポイントとSwiftUI統合
- Xcodeプロジェクト設定
- `/shared`からコンパイルされたSharedフレームワークをリンク

**重要な原則**: ビジネスロジックは`shared/`、UIは`composeApp/`に配置。この分離を守ること。

## ビルドと開発コマンド

### Android

```bash
# デバッグAPKをビルド
./gradlew :composeApp:assembleDebug

# 接続されたデバイスにインストール・実行
./gradlew :composeApp:installDebug

# テスト実行
./gradlew :shared:testDebugUnitTest
./gradlew :composeApp:testDebugUnitTest
```

### iOS

```bash
# コマンドラインからビルド（Xcodeが必要）
cd iosApp
xcodebuild -project iosApp.xcodeproj -scheme iosApp -configuration Debug

# またはXcodeで開く
open iosApp/iosApp.xcodeproj
```

### その他のGradleタスク

```bash
# 利用可能なタスク一覧
./gradlew tasks

# クリーンビルド
./gradlew clean
```

## 設計ドキュメント

全ての設計ドキュメントは`/docs/design/`に配置：

- **ペルソナ.md**: ユーザーペルソナ（主要: 31歳ITエンジニア）
- **ドメインモデル図.md**: ドメインモデル図
- **機能一覧.md**: 機能リストとフェーズ計画（Phase 1-3）
- **画面設計_ネイティブアプリ.md**: 画面設計仕様
- **ジャーニーマップ.html**: ユーザージャーニーマップ

**重要**: これらのドキュメントがプロダクト要件の情報源。機能実装やアーキテクチャ決定時は必ず参照すること。

**同期について**: 設計ドキュメントはGoogle Driveと同期管理。Serverチームが更新した場合はこのリポジトリに反映する（`/docs/README.md`参照）。

## API仕様

**API設計書**: https://api.inspirehub.wtnqk.org/docs

- **ベースURL**: `http://localhost:8787`（開発環境）
- **認証**: Google OAuth → JWT Bearer token
- **主要エンドポイント**:
  - 認証: `/auth/google/url`, `/auth/google/callback`, `/auth/me`
  - ノード: `GET/POST /nodes`, `GET/PUT/DELETE /nodes/{id}`, `POST /nodes/{id}/like`
  - コメント: `GET/POST /nodes/{nodeId}/comments`, `PUT/DELETE /comments/{id}`
  - タグ: `GET /tags/popular`, `GET /tags/suggest`

**重要**: ネットワーク層実装時は必ずAPI設計書を参照すること。

## 主要機能（Phase 1優先度）

Phase 1で実装する機能：

1. **認証**: SSO連携（Google/Microsoft）
2. **投稿管理**:
   - 課題投稿
   - アイデア投稿
   - 派生（引用）アイデア投稿
3. **エンゲージメント**:
   - リアクション（👍 いいね、💡 共感、👀 気になる、🤝 作ってみたい）
   - コメント
4. **発見機能**:
   - フィード表示（新着/ホット/人気）
   - アイデアの繋がりをマップで可視化

詳細は`docs/design/機能一覧.md`を参照。

## アーキテクチャ

**詳細は`docs/architecture.md`を参照してください。**

Phase 1で採用するアーキテクチャパターン: **MVVM + Store Pattern**

```
ViewModel (per-screen, disposable)
  ├→ Store (memory state, singleton, concrete class)
  └→ Repository (persistence, singleton, interface + impl)
```

### モジュール依存関係
```
composeApp → shared
iosApp → shared (Shared.frameworkを経由)
```

### 主要技術スタック
- **State Management**: ViewModel + StateFlow
- **DI**: Koin
- **Network**: Ktor Client + Mock
- **Persistence**: DataStore（Phase 1）
- **ViewModel共有**: KMP-ObservableViewModel
- **非同期処理**: KMP-NativeCoroutines

### プラットフォーム固有コードのパターン
プラットフォーム固有の機能を追加する場合：
1. `shared/commonMain`で`expect`宣言を定義
2. `shared/androidMain`と`shared/iosMain`で`actual`実装を提供
3. `composeApp`の共通UIコードから利用

## 🔴 iOS開発 必須ルール

### ターゲットバージョン
- **最低ターゲット: iOS 18**（可能な限りiOS 26のAPIを活用）
- 非推奨APIの使用は禁止

### 非推奨API禁止リスト

| 使うな | 代わりにこれ | 理由 |
|--------|-------------|------|
| `NavigationView` | `NavigationStack` / `NavigationSplitView` | iOS 16で非推奨 |
| `@StateObject` (KMP VM用) | `@StateViewModel` | KMP-ObservableViewModel正規の方法 |
| `@ObservedObject` (KMP VM用) | `@ObservedViewModel` | 同上 |
| `@EnvironmentObject` (KMP VM用) | `@EnvironmentViewModel` | 同上 |
| `.onChange(of:) { newValue in }` | `.onChange(of:) { oldValue, newValue in }` | iOS 17で非推奨 |
| `ObservableObject` Wrapper | Kotlin VM直接利用 | KMP-ObservableViewModelで不要 |

### KMP-ObservableViewModel 設計方針

**ViewModelWrapperは作るな。** KMP-ObservableViewModelを使えばKotlin VMをSwiftUIから直接利用できる。

#### Kotlin側
```kotlin
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.MutableStateFlow
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState

class HomeViewModel(...) : ViewModel() {
    @NativeCoroutinesState
    val nodes: StateFlow<List<Node>> = ...

    @NativeCoroutinesState
    val isLoading: StateFlow<Boolean> = ...
}
```

#### Swift側（グローバル設定 - 1回だけ）
```swift
// KMPViewModel+Extensions.swift
import KMPObservableViewModelCore
import shared

extension Kmp_observableviewmodel_coreViewModel: @retroactive ViewModel { }
extension Kmp_observableviewmodel_coreViewModel: @retroactive Observable { }
```

#### SwiftUI View
```swift
import KMPObservableViewModelSwiftUI

struct HomeView: View {
    @StateViewModel var viewModel = HomeViewModel()  // Kotlin VMを直接使用

    var body: some View {
        NavigationStack {
            List(viewModel.nodes) { node in ... }
        }
    }
}
```

### Observation フレームワーク
- iOS 17+ の `Observation` フレームワークに対応済み（`@retroactive Observable`）
- SwiftUIは**アクセスしたプロパティだけ**を監視 → 効率的な再描画

## 開発フロー

1. **機能実装**:
   - `shared/commonMain`でドメインモデルから開始
   - 必要に応じてプラットフォーム固有実装を追加
   - `composeApp`でUIを構築
   - 両プラットフォームでテスト

2. **設計ドキュメント更新時**:
   - Google Driveで設計ドキュメントが更新されたら`/docs/design/`に同期
   - 明確な説明でコミット
   - チームに設計変更を通知

3. **コミットメッセージ**:
   - Conventional Commitsフォーマットを使用
   - 例: `feat:`, `fix:`, `docs:`, `refactor:`

4. **PR・ブランチ方針**:
   - **1PR = 1機能目的**。変更の目的が複数ある場合は別PR・別ブランチに分けること
   - **差分は200行程度が目安**。レビューしやすさを最優先する
   - エージェントチームの作業効率よりも、機能単位の分割を優先する
   - 既存PRに無関係な機能を相乗りさせない
   - **Android専用の変更は `android/` プレフィックスを使う**（例: `android/feat-xxx`, `android/fix-xxx`）
     → XcodeCloudが不要にトリガーされるのを防ぐ（詳細: `.claude/rules/team-workflow.md`）

## GitHub Actions（Issue駆動開発）

GitHub IssueやPRコメントで `@claude` メンションするとClaude Codeが起動する（Collaborator以上のみ）。

- **方針確認**: `@claude 対応方針を考えて` → 調査・Plan投稿（何往復でもOK）
- **実装依頼**: `@claude /implement` → Issueコメントの文脈を読んで実装→PR→TestFlight配信

## 依存関係

主要ライブラリ（バージョンは`gradle/libs.versions.toml`参照）:
- Kotlin: 2.3.0
- Compose Multiplatform: 1.10.0
- Android Gradle Plugin: 8.11.2
- Lifecycle ViewModel: 2.9.6

## テスト戦略

### ViewModelのテスト（必須）

**重要**: ViewModelは必ず単体テストを書くこと。

#### テスト実装パターン

```kotlin
// 1. MainDispatcherRuleを継承
class MyViewModelTest : MainDispatcherRule() {

    @BeforeTest
    fun setup() {
        // 2. Fake実装を使う（MockKは使わない）
        fakeRepository = FakeMyRepository()
        viewModel = MyViewModel(fakeRepository)
    }

    @Test
    fun `テスト名 - 日本語で書ける`() = runTest {
        // 3. runTestを使う
        // 4. Given-When-Then パターン
    }
}
```

#### 重要な原則

1. **MockKを使わない**: KMPではKotlin/Nativeで不安定。Fake実装を使う
2. **MainDispatcherRule**: `shared/src/commonTest/kotlin/.../test/MainDispatcherRule.kt`を継承
3. **viewModelScope.launch**: ViewModelで使う（Android公式推奨）。Repositoryはsuspend関数
4. **StateFlowのテスト**: Turbineライブラリの`.test { }`を使用
5. **テスト名**: バッククォートで日本語可（例: `` `logout - ログアウトが成功すること` ``）

#### テストライブラリ

- `kotlin.test`: 標準テストフレームワーク
- `kotlinx-coroutines-test`: コルーチンテスト（runTest, MainDispatcher置換）
- `turbine`: StateFlow/Flowのテスト

参考: `AuthViewModelTest.kt`

## 用語集

プロジェクト固有の用語：

- **課題**: ユーザーが投稿する「解決したい問題」や「作りたいもの」
- **アイデア**: 課題に対する解決策、または単独のアイデア
- **派生アイデア**: 既存の課題やアイデアから引用して作成されたアイデア
- **作ってみたい**: ユーザーがアイデアに対して示す軽いコミットメント（参加確定の手前）
- **ノード**: 課題またはアイデアの総称（マップ表示での呼称）

## 開発自動化ツール

### カスタムエージェント（`.claude/agents/`）

Task toolでspawnして使用。

| エージェント | 用途 |
| ----------- | ---- |
| kotlin-dev | shared層のKotlin実装 |
| ios-dev | iosApp/のSwiftUI実装 |
| architect | アーキテクチャ設計・技術選定 |
| qa-checker | コード品質検証・テスト実行 |
| design-reviewer | UI設計書準拠・HIG・審査チェック |
| code-reviewer | Kotlin+iOS統合コードレビュー |
| task-planner | タスク分解・ガントチャート・チーム管理 |

### スキル（`.claude/skills/`）

| コマンド | 用途 |
| ------- | ---- |
| /implement | Issue駆動実装（Plan→承認→PR→TestFlight配信） |
| /qa-check [BUG-ID\|all] | QAチェックリスト検証 |
| /build-check [ios\|android\|shared\|all] | ビルド実行・エラー報告 |
| /fix-bug \<BUG-ID\> | バグ自動修正 |
| /design-review [画面名\|all] | デザインレビュー |
| /retro | 振り返り・効率化提案 |
