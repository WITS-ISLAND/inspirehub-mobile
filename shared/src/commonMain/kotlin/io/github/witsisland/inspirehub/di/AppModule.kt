package io.github.witsisland.inspirehub.di

import io.github.witsisland.inspirehub.data.network.createHttpClient
import io.github.witsisland.inspirehub.data.repository.AuthRepositoryImpl
import io.github.witsisland.inspirehub.data.source.AuthDataSource
import io.github.witsisland.inspirehub.data.source.CommentDataSource
import io.github.witsisland.inspirehub.data.source.KtorAuthDataSource
import io.github.witsisland.inspirehub.data.source.KtorCommentDataSource
import io.github.witsisland.inspirehub.data.source.KtorNodeDataSource
import io.github.witsisland.inspirehub.data.source.NodeDataSource
import io.github.witsisland.inspirehub.domain.repository.AuthRepository
import io.github.witsisland.inspirehub.domain.store.UserStore
import io.github.witsisland.inspirehub.presentation.viewmodel.AuthViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * Koin DIモジュール
 */
val appModule = module {
    // Store（シングルトン、具象クラス）
    singleOf(::UserStore)

    // HttpClient（シングルトン）
    single {
        val userStore: UserStore = get()
        createHttpClient(
            baseUrl = "https://api.inspirehub.wtnqk.org",
            enableLogging = true,
            tokenProvider = { userStore.getAccessToken() }
        )
    }

    // DataSource（シングルトン）
    single<AuthDataSource> { KtorAuthDataSource(get(), get()) }
    single<NodeDataSource> { KtorNodeDataSource(get()) }
    single<CommentDataSource> { KtorCommentDataSource(get()) }

    // Repository（シングルトン、インターフェース）
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }

    // ViewModel（Factory - 画面ごとに生成）
    factoryOf(::AuthViewModel)
}
