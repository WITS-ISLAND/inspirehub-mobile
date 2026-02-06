# SPMパッケージ追加 + Xcode Cloud対応レポート

> **Date**: 2026-02-02
> **Context**: cmd_007 ViewModelWrapper全廃に伴うSPMパッケージ追加

## 1. やること（具体的な手順）

### STEP A: Xcodeで2つのSPMパッケージを追加

**Xcode GUI操作**（殿が実施）:

1. `iosApp/iosApp.xcodeproj` をXcodeで開く
2. Project Navigator > プロジェクト選択 > 「Package Dependencies」タブ
3. 「+」ボタンで以下2つを追加:

| # | パッケージURL | バージョン | 追加するProduct |
|---|-------------|-----------|----------------|
| 1 | `https://github.com/rickclephas/KMP-ObservableViewModel.git` | 1.0.1 (Up to Next Major) | `KMPObservableViewModelCore`, `KMPObservableViewModelSwiftUI` |
| 2 | `https://github.com/rickclephas/KMP-NativeCoroutines.git` | 1.0.0 (Up to Next Major) | `KMPNativeCoroutinesCore`, `KMPNativeCoroutinesAsync` |

4. 追加後、自動的に以下が更新される:
   - `project.pbxproj`（SPM参照・ビルドフェーズ）
   - `Package.resolved`（依存解決結果）

5. ビルド確認: Cmd+B

**所要時間**: 5分程度

### STEP B: Swift側グローバル設定ファイル作成

足軽が実施（STEP3のタスク）:

```swift
// iosApp/iosApp/KMPViewModel+Extensions.swift
import KMPObservableViewModelCore
import shared

extension Kmp_observableviewmodel_coreViewModel: @retroactive ViewModel { }
extension Kmp_observableviewmodel_coreViewModel: @retroactive Observable { }
```

### STEP C: 各ViewでWrapper削除 → Kotlin VM直接利用

足軽が実施（STEP4のタスク）。SPMパッケージが追加されていることが前提。

---

## 2. Xcode Cloudとの関係

### 現状の問題（docs/ci_cd_strategy.md より）

| 問題 | 詳細 |
|------|------|
| JDK未搭載 | Xcode CloudにJava/JDK がない → Gradleビルド不可 → Shared framework生成不可 |
| SPMパッケージ | SPM依存解決自体はXcode Cloudで問題なし（GoogleSignIn-iOSは既に動作実績あり） |

### 今回追加するSPMパッケージの影響

**KMP-ObservableViewModel / KMP-NativeCoroutines のSPMパッケージは、Swift側のラッパーライブラリのみ。** Kotlinコンパイルは含まれない。

```
SPMパッケージ（Swift only）:
  KMPObservableViewModelCore    → Swift protocol/extension のみ
  KMPObservableViewModelSwiftUI → @StateViewModel 等の property wrapper
  KMPNativeCoroutinesCore       → Swift側の型定義のみ
  KMPNativeCoroutinesAsync      → Swift async/await bridge

Kotlin側（Gradle依存）:
  kmp-observableviewmodel-core  → ViewModel base class（shared/build.gradle.kts）
  kmp-nativecoroutines          → @NativeCoroutinesState 等（shared/build.gradle.kts）
```

つまり:
- **SPMパッケージ追加 → Xcode Cloudへの悪影響なし**（SPM依存解決は正常に動く）
- **Kotlin側のビルド問題は既存のまま**（JDK未搭載問題は変わらない）

### Xcode Cloudでのビルドフロー

```
┌─ Xcode Cloud ─────────────────────────────────┐
│                                                │
│  1. git clone                                  │
│  2. ci_post_clone.sh 実行                      │
│     └→ JDKインストール + Gradleビルド            │
│        (Shared.framework 生成)                  │
│  3. SPM依存解決                                 │
│     └→ GoogleSignIn-iOS ✅                      │
│     └→ KMP-ObservableViewModel ✅ ← 【新規】    │
│     └→ KMP-NativeCoroutines ✅ ← 【新規】       │
│  4. xcodebuild (iOSアプリビルド)                 │
│     └→ Shared.framework (step 2) + SPMライブラリ │
│  5. TestFlight配信                              │
│                                                │
└────────────────────────────────────────────────┘
```

**結論: SPMパッケージ追加とXcode Cloud JDK問題は独立した問題。互いに影響しない。**

---

## 3. Xcode Cloud対応（ci_post_clone.sh）の現状と次のアクション

### 現状

`ci_scripts/ci_post_clone.sh` は**まだ未作成**。Phase 1推奨の案Aが未実施。

### 必要なアクション（優先度順）

| # | アクション | 担当 | ブロッカー |
|---|-----------|------|-----------|
| 1 | SPMパッケージ追加（上記STEP A） | 殿（Xcode GUI） | なし |
| 2 | `ci_scripts/ci_post_clone.sh` 作成（案A） | 足軽に振れる | Apple Developer Program加入確認 |
| 3 | Xcode Cloud ワークフロー設定 | 殿（Apple Developer設定） | Apple Developer Program |
| 4 | TestFlight配信テスト | 殿 + 将軍 | #1, #2, #3 全完了 |

### ci_post_clone.sh の内容（案A: 即時対応）

```bash
#!/bin/sh
set -e

echo "=== Installing JDK 17 ==="
brew install --quiet openjdk@17
export JAVA_HOME=$(brew --prefix openjdk@17)
export PATH="$JAVA_HOME/bin:$PATH"
java -version

echo "=== Building Shared Framework ==="
cd "$CI_PRIMARY_REPOSITORY_PATH"
./gradlew :shared:embedAndSignAppleFrameworkForXcode \
    -Pkotlin.apple.xcodeCompatibleFrameworkName=Shared
```

**注意**: `embedAndSignAppleFrameworkForXcode` はXcodeのビルドフェーズで自動呼び出しされるため、ci_post_clone.sh ではJDKインストールのみで十分な場合もある。実際のXcode Cloudログで確認が必要。

---

## 4. まとめ

| 項目 | 状態 | 影響 |
|------|------|------|
| SPMパッケージ追加 | 殿のXcode操作で5分 | Xcode Cloudに悪影響なし |
| ci_post_clone.sh | 未作成 | これがないとXcode Cloudでビルド不可 |
| Wrapper全廃リファクタ | cmd_007で進行中 | SPM追加が前提 |
| TestFlight配信 | Apple Developer Program依存 | 期限: 2/9 |

**殿への依頼**:
1. Xcodeで2つのSPMパッケージを追加してください（STEP A）
2. Apple Developer Programの状況を教えてください（Xcode Cloud利用に必要）
