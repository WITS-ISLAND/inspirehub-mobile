package io.github.witsisland.inspirehub.di

import io.github.witsisland.inspirehub.presentation.viewmodel.AuthViewModel
import io.github.witsisland.inspirehub.presentation.viewmodel.DetailViewModel
import io.github.witsisland.inspirehub.presentation.viewmodel.DiscoverViewModel
import io.github.witsisland.inspirehub.presentation.viewmodel.HomeViewModel
import io.github.witsisland.inspirehub.presentation.viewmodel.MapViewModel
import io.github.witsisland.inspirehub.presentation.viewmodel.MyPageViewModel
import io.github.witsisland.inspirehub.presentation.viewmodel.PostViewModel
import io.github.witsisland.inspirehub.presentation.viewmodel.ReactionUsersViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * iOSからKoinを使うためのヘルパー
 */
object KoinHelper : KoinComponent {
    fun getAuthViewModel(): AuthViewModel {
        val viewModel: AuthViewModel by inject()
        return viewModel
    }

    fun getHomeViewModel(): HomeViewModel {
        val viewModel: HomeViewModel by inject()
        return viewModel
    }

    fun getMapViewModel(): MapViewModel {
        val viewModel: MapViewModel by inject()
        return viewModel
    }

    fun getMyPageViewModel(): MyPageViewModel {
        val viewModel: MyPageViewModel by inject()
        return viewModel
    }

    fun getPostViewModel(): PostViewModel {
        val viewModel: PostViewModel by inject()
        return viewModel
    }

    fun getDetailViewModel(): DetailViewModel {
        val viewModel: DetailViewModel by inject()
        return viewModel
    }

    fun getDiscoverViewModel(): DiscoverViewModel {
        val viewModel: DiscoverViewModel by inject()
        return viewModel
    }

    fun getReactionUsersViewModel(): ReactionUsersViewModel {
        val viewModel: ReactionUsersViewModel by inject()
        return viewModel
    }
}
