# InspireHub Mobile アーキテクチャ設計書

## 概要

このドキュメントは、InspireHub Mobile Phase 1（3-5日スプリント）で採用するアーキテクチャと技術スタックを定義します。

## 技術スタック

### コアライブラリ

- **State Management**: ViewModel + StateFlow（標準アプローチ）
- **DI**: Koin 4.1.0
- **Network**: Ktor Client 3.0.3
- **Persistence**: DataStore（Phase 1）
- **ViewModel共有**: [KMP-ObservableViewModel](https://github.com/rickclephas/KMP-ObservableViewModel) 1.0.0-BETA-10
- **iOS State Observation**: Swift Observation framework - Timer-based StateFlow polling (KMP-NativeCoroutinesは互換性の問題により不使用)

### Navigation

- **iOS**: SwiftUI Navigation（ネイティブ実装）
- **Android**: Compose Navigation（別エンジニアが後で実装）

### アーキテクチャパターン

**MVVM + Store Pattern**

```
ViewModel (per-screen, disposable)
  ├→ Store (memory state, singleton, concrete class)
  └→ Repository (persistence, singleton, interface + impl)
```

## アーキテクチャ詳細

### Store Pattern（状態管理）

**責務**: 画面を跨ぐメモリ内状態の保持

- **実装**: 具象クラス（インターフェースなし）
- **スコープ**: シングルトン
- **用途**: 画面間で共有する一時的な状態（例: アイデアリスト、フィルタ状態）

**命名規則**: `{Domain}Store`（例: `IdeaStore`, `UserStore`）

**実装例**:

```kotlin
// shared/commonMain/kotlin/io/github/witsisland/inspirehub/domain/store/IdeaStore.kt
package io.github.witsisland.inspirehub.domain.store

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import io.github.witsisland.inspirehub.domain.model.Idea

class IdeaStore {
    private val _ideas = MutableStateFlow<List<Idea>>(emptyList())
    val ideas: StateFlow<List<Idea>> = _ideas.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun updateIdeas(ideas: List<Idea>) {
        _ideas.value = ideas
    }

    fun addIdea(idea: Idea) {
        _ideas.value = _ideas.value + idea
    }

    fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }

    fun clear() {
        _ideas.value = emptyList()
    }
}
```

### Repository Pattern（永続化）

**責務**: データの永続化と取得

- **実装**: インターフェース + 実装クラス
- **スコープ**: シングルトン
- **用途**: DataStore/DB/APIへのアクセス

**命名規則**:
- インターフェース: `{Domain}Repository`（例: `IdeaRepository`）
- 実装クラス: `{Domain}RepositoryImpl`（例: `IdeaRepositoryImpl`）

**実装例**:

```kotlin
// shared/commonMain/kotlin/io/github/witsisland/inspirehub/domain/repository/IdeaRepository.kt
package io.github.witsisland.inspirehub.domain.repository

import io.github.witsisland.inspirehub.domain.model.Idea

interface IdeaRepository {
    suspend fun getIdeas(): Result<List<Idea>>
    suspend fun getIdea(id: String): Result<Idea>
    suspend fun saveIdea(idea: Idea): Result<Unit>
    suspend fun deleteIdea(id: String): Result<Unit>
}
```

```kotlin
// shared/commonMain/kotlin/io/github/witsisland/inspirehub/data/repository/IdeaRepositoryImpl.kt
package io.github.witsisland.inspirehub.data.repository

import io.github.witsisland.inspirehub.domain.repository.IdeaRepository
import io.github.witsisland.inspirehub.domain.model.Idea
import io.github.witsisland.inspirehub.data.source.IdeaDataSource

class IdeaRepositoryImpl(
    private val dataSource: IdeaDataSource
) : IdeaRepository {
    override suspend fun getIdeas(): Result<List<Idea>> {
        return try {
            val ideas = dataSource.getIdeas()
            Result.success(ideas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getIdea(id: String): Result<Idea> {
        return try {
            val idea = dataSource.getIdea(id)
            Result.success(idea)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveIdea(idea: Idea): Result<Unit> {
        return try {
            dataSource.saveIdea(idea)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteIdea(id: String): Result<Unit> {
        return try {
            dataSource.deleteIdea(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### ViewModel Pattern

**責務**: UI状態の管理とビジネスロジックの呼び出し

- **実装**: KMP-ObservableViewModelの`ViewModel`を継承
- **スコープ**: 画面ごと（Factory）
- **用途**: Storeの監視、Repositoryの呼び出し、UI状態の管理

**実装例**:

```kotlin
// shared/commonMain/kotlin/io/github/witsisland/inspirehub/presentation/viewmodel/IdeaListViewModel.kt
package io.github.witsisland.inspirehub.presentation.viewmodel

import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.MutableStateFlow
import com.rickclephas.kmp.observableviewmodel.coroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import io.github.witsisland.inspirehub.domain.store.IdeaStore
import io.github.witsisland.inspirehub.domain.repository.IdeaRepository
import io.github.witsisland.inspirehub.domain.model.Idea

class IdeaListViewModel(
    private val store: IdeaStore,
    private val repository: IdeaRepository
) : ViewModel() {

    // StoreからUI状態を取得（監視）
    val ideas: StateFlow<List<Idea>> = store.ideas
    val isLoading: StateFlow<Boolean> = store.isLoading

    // エラー状態は画面固有
    val error = MutableStateFlow(viewModelScope, null as String?)

    fun loadIdeas(forceRefresh: Boolean = false) {
        // キャッシュがあれば再取得しない
        if (!forceRefresh && ideas.value.isNotEmpty()) return

        viewModelScope.launch {
            store.setLoading(true)
            error.value = null

            when (val result = repository.getIdeas()) {
                is Result.Success -> {
                    store.updateIdeas(result.getOrNull() ?: emptyList())
                }
                is Result.Failure -> {
                    error.value = result.exceptionOrNull()?.message ?: "Unknown error"
                }
            }

            store.setLoading(false)
        }
    }

    fun refresh() {
        loadIdeas(forceRefresh = true)
    }
}
```

```kotlin
// shared/commonMain/kotlin/io/github/witsisland/inspirehub/presentation/viewmodel/IdeaPostViewModel.kt
package io.github.witsisland.inspirehub.presentation.viewmodel

import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.MutableStateFlow
import com.rickclephas.kmp.observableviewmodel.coroutineScope
import kotlinx.coroutines.launch
import io.github.witsisland.inspirehub.domain.store.IdeaStore
import io.github.witsisland.inspirehub.domain.repository.IdeaRepository
import io.github.witsisland.inspirehub.domain.model.Idea

class IdeaPostViewModel(
    private val store: IdeaStore,
    private val repository: IdeaRepository
) : ViewModel() {

    val title = MutableStateFlow(viewModelScope, "")
    val description = MutableStateFlow(viewModelScope, "")
    val isSubmitting = MutableStateFlow(viewModelScope, false)
    val error = MutableStateFlow(viewModelScope, null as String?)
    val isSuccess = MutableStateFlow(viewModelScope, false)

    fun updateTitle(value: String) {
        title.value = value
    }

    fun updateDescription(value: String) {
        description.value = value
    }

    fun submit() {
        viewModelScope.launch {
            isSubmitting.value = true
            error.value = null

            val idea = Idea(
                id = generateId(), // UUID生成など
                title = title.value,
                description = description.value,
                createdAt = currentTimestamp()
            )

            when (val result = repository.saveIdea(idea)) {
                is Result.Success -> {
                    // Storeに追加して画面間で即座に反映
                    store.addIdea(idea)
                    isSuccess.value = true
                }
                is Result.Failure -> {
                    error.value = result.exceptionOrNull()?.message ?: "Failed to save"
                }
            }

            isSubmitting.value = false
        }
    }
}
```

### DI構成（Koin）

```kotlin
// shared/commonMain/kotlin/io/github/witsisland/inspirehub/di/AppModule.kt
package io.github.witsisland.inspirehub.di

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import io.github.witsisland.inspirehub.domain.store.IdeaStore
import io.github.witsisland.inspirehub.domain.repository.IdeaRepository
import io.github.witsisland.inspirehub.data.repository.IdeaRepositoryImpl
import io.github.witsisland.inspirehub.data.source.IdeaDataSource
import io.github.witsisland.inspirehub.data.source.MockIdeaDataSource
import io.github.witsisland.inspirehub.presentation.viewmodel.IdeaListViewModel
import io.github.witsisland.inspirehub.presentation.viewmodel.IdeaPostViewModel

val appModule = module {
    // Store（シングルトン、具象クラス）
    singleOf(::IdeaStore)

    // Repository（シングルトン、インターフェース）
    single<IdeaRepository> { IdeaRepositoryImpl(get()) }

    // DataSource（Phase 1はモック）
    single<IdeaDataSource> { MockIdeaDataSource() }

    // ViewModel（Factory - 画面ごとに生成）
    factoryOf(::IdeaListViewModel)
    factoryOf(::IdeaPostViewModel)
}
```

## モジュール構成

```
shared/
├── commonMain/
│   └── kotlin/io/github/witsisland/inspirehub/
│       ├── domain/
│       │   ├── model/          # ドメインモデル（Idea, User, etc.）
│       │   ├── store/          # Store（具象クラス）
│       │   └── repository/     # Repositoryインターフェース
│       ├── data/
│       │   ├── repository/     # Repository実装クラス
│       │   └── source/         # DataSource（Mock/Real）
│       ├── presentation/
│       │   └── viewmodel/      # ViewModel
│       └── di/
│           └── AppModule.kt    # DIモジュール定義
├── androidMain/
│   └── kotlin/                 # Android固有実装（expect/actual）
└── iosMain/
    └── kotlin/                 # iOS固有実装（expect/actual）
```

## iOS統合

### StateFlow観測パターン（Timer-based Polling）

KMP-NativeCoroutinesは互換性の問題により使用せず、Timer-based pollingでStateFlowを観測します。

**実装パターン**:

```swift
import SwiftUI
import Shared
import Combine

class AuthViewModelWrapper: ObservableObject {
    private let viewModel: AuthViewModel
    private var cancellables = Set<AnyCancellable>()

    @Published var currentUser: User? = nil
    @Published var isAuthenticated: Bool = false
    @Published var isLoading: Bool = false
    @Published var error: String? = nil

    init() {
        // Koinから共有ViewModelを取得
        self.viewModel = KoinHelper().getAuthViewModel()

        // StateFlowをポーリング監視（0.1秒間隔）
        observeViewModel()
    }

    private func observeViewModel() {
        Timer.publish(every: 0.1, on: .main, in: .common)
            .autoconnect()
            .sink { [weak self] _ in
                guard let self = self else { return }
                // StateFlowの現在値を@Publishedプロパティにコピー
                self.currentUser = self.viewModel.currentUser.value as? User
                self.isAuthenticated = self.viewModel.isAuthenticated.value as! Bool
                self.isLoading = self.viewModel.isLoading.value as! Bool
                self.error = self.viewModel.error.value as? String
            }
            .store(in: &cancellables)
    }

    // ViewModelのメソッドを委譲
    func login() {
        viewModel.login()
    }
}
```

**ポイント**:
- `@ObservableObject`でSwiftUIのObservation frameworkを活用
- `Timer.publish`で0.1秒ごとにStateFlowの値を取得
- `@Published`プロパティに値をコピーしてSwiftUIに反映
- ViewModelのメソッドはラッパーから委譲

**注意点**:
- iOS 17+のObservation frameworkを使用（`@Observable`マクロは不使用）
- ポーリング間隔は0.1秒（パフォーマンスと反応速度のバランス）
- メモリリーク防止のため`[weak self]`を使用

### SwiftUIでの使用方法

```swift
// iosApp/LoginView.swift
import SwiftUI

struct LoginView: View {
    @ObservedObject var viewModel: AuthViewModelWrapper

    var body: some View {
        VStack(spacing: 32) {
            Text("InspireHub")
                .font(.largeTitle)
                .fontWeight(.bold)

            Button(action: {
                viewModel.getGoogleAuthUrl()
            }) {
                HStack {
                    Image(systemName: "g.circle.fill")
                    Text("Googleでログイン")
                }
                .font(.headline)
                .foregroundColor(.white)
                .frame(maxWidth: .infinity)
                .padding()
                .background(Color.blue)
                .cornerRadius(12)
            }
            .disabled(viewModel.isLoading)

            if viewModel.isLoading {
                ProgressView()
            }

            if let error = viewModel.error {
                Text(error)
                    .foregroundColor(.red)
            }
        }
    }
}

// iosApp/RootView.swift
import SwiftUI

struct RootView: View {
    @StateObject private var viewModel = AuthViewModelWrapper()

    var body: some View {
        Group {
            if viewModel.isAuthenticated {
                HomeView()
            } else {
                LoginView(viewModel: viewModel)
            }
        }
    }
}
```

### Koin初期化（iOS）

```swift
// iosApp/iOSApp.swift
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
```

**KoinHelper**（共有ViewModelの取得）:

```kotlin
// shared/iosMain/kotlin/io/github/witsisland/inspirehub/di/KoinHelper.kt
package io.github.witsisland.inspirehub.di

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import io.github.witsisland.inspirehub.presentation.viewmodel.AuthViewModel

object KoinHelper : KoinComponent {
    fun getAuthViewModel(): AuthViewModel {
        val viewModel: AuthViewModel by inject()
        return viewModel
    }
}
```

## Store vs Repository の使い分け

### Store を使用するケース

- 画面を跨いで共有するメモリ内状態
- 一覧→詳細のような画面遷移でデータを再利用したい
- フィルタ状態など、セッション内のみ有効な状態

**例**: `IdeaStore`（アイデアリスト、読み込み状態）

### Repository を使用するケース

- データの永続化が必要
- API通信が必要
- アプリ再起動後も保持したいデータ

**例**: `IdeaRepository`（アイデアの保存・取得・削除）

### 両方使う例

下書き機能を実装する場合:

1. **一時的な入力状態**: ViewModelのローカル状態（`MutableStateFlow`）
2. **画面を跨ぐ下書き共有**: `DraftStore`（具象クラス、シングルトン）
3. **下書きの永続化**: `DraftRepository`（インターフェース + 実装）

```kotlin
// DraftStore.kt - 画面間で下書きを共有
class DraftStore {
    private val _currentDraft = MutableStateFlow<Draft?>(null)
    val currentDraft: StateFlow<Draft?> = _currentDraft.asStateFlow()

    fun saveDraft(draft: Draft) {
        _currentDraft.value = draft
    }
}

// DraftRepository.kt - 下書きの永続化
interface DraftRepository {
    suspend fun saveDraft(draft: Draft): Result<Unit>
    suspend fun getDrafts(): Result<List<Draft>>
}
```

## 設計判断の理由

### UseCaseレイヤーを導入しない理由（Phase 1）

- **シンプルさ優先**: Phase 1の機能（投稿、一覧、詳細）はビジネスロジックがシンプル
- **ViewModel → Repository で十分**: 複数Repositoryをまたぐ複雑な処理が現時点で存在しない
- **YAGNI原則**: 必要になった時点で追加する（Phase 2以降で検討）

### Store を具象クラスにした理由（インターフェースなし）

- **YAGNI原則**: Phase 1（3-5日スプリント）で実装を切り替える必要がない
- **シンプルさ**: 抽象化のコストに見合うメリットがない
- **将来の拡張**: 必要になった時点で抽象化すれば良い

### Repository-centric 設計を採用した理由

- **iOS/Android の状態共有**: 両プラットフォームで同じRepositoryインスタンスを参照
- **ViewModelの軽量化**: ViewModelは画面ごとに生成・破棄できる
- **シンプルな実装**: ViewModel → Store/Repository の単方向データフロー

## Phase 1 実装スコープ

以下の機能でアーキテクチャを検証:

1. **認証**: 仮認証（ハードコードユーザー）
2. **投稿**: 課題・アイデア投稿機能
3. **一覧**: アイデアリスト表示
4. **データソース**: モックデータ（APIスペック未確定のため）

## 参考リンク

- [KMP-ObservableViewModel](https://github.com/rickclephas/KMP-ObservableViewModel)
- [KMP-NativeCoroutines](https://github.com/rickclephas/KMP-NativeCoroutines)
- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)
- [Koin](https://insert-koin.io/)
