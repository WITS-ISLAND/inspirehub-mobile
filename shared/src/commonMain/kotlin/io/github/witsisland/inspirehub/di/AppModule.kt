package io.github.witsisland.inspirehub.di

import io.github.witsisland.inspirehub.data.network.createHttpClient
import io.github.witsisland.inspirehub.data.repository.AuthRepositoryImpl
import io.github.witsisland.inspirehub.data.repository.CommentRepositoryImpl
import io.github.witsisland.inspirehub.data.repository.NodeRepositoryImpl
import io.github.witsisland.inspirehub.data.repository.ReactionRepositoryImpl
import io.github.witsisland.inspirehub.data.repository.TagRepositoryImpl
import io.github.witsisland.inspirehub.data.source.AuthDataSource
import io.github.witsisland.inspirehub.data.source.CommentDataSource
import io.github.witsisland.inspirehub.data.source.KtorAuthDataSource
import io.github.witsisland.inspirehub.data.source.KtorCommentDataSource
import io.github.witsisland.inspirehub.data.source.KtorNodeDataSource
import io.github.witsisland.inspirehub.data.source.MockCommentDataSource
import io.github.witsisland.inspirehub.data.source.MockNodeDataSource
import io.github.witsisland.inspirehub.data.source.MockReactionDataSource
import io.github.witsisland.inspirehub.data.source.MockTagDataSource
import io.github.witsisland.inspirehub.data.source.NodeDataSource
import io.github.witsisland.inspirehub.data.source.ReactionDataSource
import io.github.witsisland.inspirehub.data.source.TagDataSource
import io.github.witsisland.inspirehub.domain.repository.AuthRepository
import io.github.witsisland.inspirehub.domain.repository.CommentRepository
import io.github.witsisland.inspirehub.domain.repository.NodeRepository
import io.github.witsisland.inspirehub.domain.repository.ReactionRepository
import io.github.witsisland.inspirehub.domain.repository.TagRepository
import io.github.witsisland.inspirehub.domain.store.DiscoverStore
import io.github.witsisland.inspirehub.domain.store.NodeStore
import io.github.witsisland.inspirehub.domain.store.UserStore
import io.github.witsisland.inspirehub.presentation.viewmodel.AuthViewModel
import io.github.witsisland.inspirehub.presentation.viewmodel.DetailViewModel
import io.github.witsisland.inspirehub.presentation.viewmodel.DiscoverViewModel
import io.github.witsisland.inspirehub.presentation.viewmodel.HomeViewModel
import io.github.witsisland.inspirehub.presentation.viewmodel.MapViewModel
import io.github.witsisland.inspirehub.presentation.viewmodel.MyPageViewModel
import io.github.witsisland.inspirehub.presentation.viewmodel.PostViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * Koin DIモジュール
 */
val appModule = module {
    // Store（シングルトン、具象クラス）
    singleOf(::UserStore)
    singleOf(::NodeStore)
    singleOf(::DiscoverStore)

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
    single<AuthDataSource> { KtorAuthDataSource(get()) }
    single<NodeDataSource> { MockNodeDataSource() }
    single<CommentDataSource> { MockCommentDataSource() }
    single<ReactionDataSource> { MockReactionDataSource() }
    single<TagDataSource> { MockTagDataSource() }

    // Repository（シングルトン、インターフェース）
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    single<NodeRepository> { NodeRepositoryImpl(get()) }
    single<CommentRepository> { CommentRepositoryImpl(get()) }
    single<ReactionRepository> { ReactionRepositoryImpl(get()) }
    single<TagRepository> { TagRepositoryImpl(get()) }

    // ViewModel（Factory - 画面ごとに生成）
    factoryOf(::AuthViewModel)
    factoryOf(::HomeViewModel)
    factoryOf(::MapViewModel)
    factoryOf(::MyPageViewModel)
    factoryOf(::PostViewModel)
    factoryOf(::DetailViewModel)
    factoryOf(::DiscoverViewModel)
}
