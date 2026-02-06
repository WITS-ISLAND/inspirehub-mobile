# InspireHub Mobile

社内ハッカソンの参加障壁を下げるためのKotlin Multiplatform（KMP）モバイルアプリ。

アイデアや課題を気軽に投稿でき、「作ってみたい」という軽いコミットメントでチーム形成を支援します。

## プロジェクト構成

| ディレクトリ | 説明 |
| ----------- | ---- |
| [/shared](./shared/src) | プラットフォーム共通のビジネスロジック・ドメインモデル（Kotlin） |
| [/composeApp](./composeApp/src) | Android UI（Compose Multiplatform） |
| [/iosApp](./iosApp) | iOS UI（SwiftUI） |
| [/docs](./docs) | 設計ドキュメント |

## 技術スタック

- **Kotlin Multiplatform** (KMP) — shared層でビジネスロジックを共有
- **Compose Multiplatform** — Android UI
- **SwiftUI** — iOS UI（iOS 18+）
- **KMP-ObservableViewModel** — Kotlin ViewModelをSwiftUIから直接利用
- **Koin** — DI
- **Ktor Client** — ネットワーク

## ビルド

### Android

```bash
./gradlew :composeApp:assembleDebug
```

### iOS

Xcodeで `iosApp/iosApp.xcodeproj` を開いてビルド。

### テスト

```bash
# shared層のユニットテスト
./gradlew :shared:testDebugUnitTest
```

## アーキテクチャ

**MVVM + Store Pattern** を採用。詳細は [docs/architecture.md](./docs/architecture.md) を参照。

```text
ViewModel (per-screen, disposable)
  ├→ Store (memory state, singleton)
  └→ Repository (persistence, singleton, interface + impl)
```

## ドキュメント

設計ドキュメントは [docs/](./docs) に配置。詳細は [docs/README.md](./docs/README.md) を参照。
