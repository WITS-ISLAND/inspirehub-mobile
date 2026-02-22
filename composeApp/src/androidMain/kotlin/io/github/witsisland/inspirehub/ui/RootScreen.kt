package io.github.witsisland.inspirehub.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import io.github.witsisland.inspirehub.presentation.viewmodel.AuthViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RootScreen(authViewModel: AuthViewModel = koinViewModel()) {
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()

    if (isAuthenticated) {
        MainScreen(authViewModel = authViewModel)
    } else {
        LoginScreen(viewModel = authViewModel)
    }
}
