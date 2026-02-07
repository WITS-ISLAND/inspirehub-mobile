---
description: Rules for editing Kotlin files in the shared KMP module
globs: ["shared/**/*.kt"]
---

# Kotlin KMP 開発ルール

## KMP-ObservableViewModel パターン

### ViewModel基底クラス
- `com.rickclephas.kmp.observableviewmodel.ViewModel` を継承せよ
- AndroidXの `ViewModel()` を直接使うな

### StateFlow
- `com.rickclephas.kmp.observableviewmodel.MutableStateFlow(viewModelScope, initialValue)` を使え
- `kotlinx.coroutines.flow.MutableStateFlow(initialValue)` は使うな（iOS側でobserveされない）
- `stateIn` も `com.rickclephas.kmp.observableviewmodel.stateIn` を使え

### @NativeCoroutinesState
- iOS側に公開する全ての `StateFlow` プロパティに `@NativeCoroutinesState` をつけよ
- `import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState`

### viewModelScope
- `viewModelScope.launch` は `com.rickclephas.kmp.observableviewmodel.launch` を使え
- KMP-ObservableViewModelの `viewModelScope` は `CoroutineScope` ではなく `ViewModelScope` 型
- `stateIn(viewModelScope, ...)` は直接使えない → ライブラリの `stateIn` を使うか `MutableStateFlow` + 手動更新パターンを使え

## アーキテクチャ: MVVM + Store Pattern
```
ViewModel (per-screen, disposable)
  ├→ Store (memory state, singleton)
  └→ Repository (persistence, singleton, interface + impl)
```

## ロギング
- `println` を使うな → **Kermit** (`co.touchlab:kermit`) を使え
- `Logger.withTag("クラス名")` でタグ付きロガーを生成
- ログレベル: `.e` (エラー), `.w` (警告), `.d` (デバッグ), `.i` (情報)
- Android → Logcat、iOS → os_log に自動ルーティングされる

## テスト
- MockKは使わない（KMP Nativeで不安定）→ Fake実装を使え
- `MainDispatcherRule` を継承
- `runTest` を使え
- StateFlowテストには Turbine の `.test { }` を使え
