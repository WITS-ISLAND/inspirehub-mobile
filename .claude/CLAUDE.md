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

### モジュール依存関係
```
composeApp → shared
iosApp → shared (Shared.frameworkを経由)
```

### プラットフォーム固有コードのパターン
プラットフォーム固有の機能を追加する場合：
1. `shared/commonMain`で`expect`宣言を定義
2. `shared/androidMain`と`shared/iosMain`で`actual`実装を提供
3. `composeApp`の共通UIコードから利用

### ナビゲーション
（実装予定 - アーキテクチャが確定したらこのセクションを更新）

### 状態管理
（実装予定 - アーキテクチャが確定したらこのセクションを更新）

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

## 依存関係

主要ライブラリ（バージョンは`gradle/libs.versions.toml`参照）:
- Kotlin: 2.3.0
- Compose Multiplatform: 1.10.0
- Android Gradle Plugin: 8.11.2
- Lifecycle ViewModel: 2.9.6

## テスト戦略

（プロジェクトの成熟に合わせて定義予定 - テストインフラが確立したらこのセクションを更新）

## 用語集

プロジェクト固有の用語：

- **課題**: ユーザーが投稿する「解決したい問題」や「作りたいもの」
- **アイデア**: 課題に対する解決策、または単独のアイデア
- **派生アイデア**: 既存の課題やアイデアから引用して作成されたアイデア
- **作ってみたい**: ユーザーがアイデアに対して示す軽いコミットメント（参加確定の手前）
- **ノード**: 課題またはアイデアの総称（マップ表示での呼称）
