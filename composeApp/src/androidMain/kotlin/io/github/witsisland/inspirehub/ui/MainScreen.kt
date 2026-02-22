package io.github.witsisland.inspirehub.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.github.witsisland.inspirehub.presentation.viewmodel.AuthViewModel
import io.github.witsisland.inspirehub.ui.screen.DetailScreen
import io.github.witsisland.inspirehub.ui.screen.IdeaPostScreen
import io.github.witsisland.inspirehub.ui.screen.IssuePostScreen
import io.github.witsisland.inspirehub.ui.screen.PostTypeSelectSheet
import kotlinx.serialization.Serializable

// ---------------------------------------------------------------------------
// ナビゲーションルート定義
// ---------------------------------------------------------------------------

/** ホーム画面ルート */
@Serializable
object HomeRoute

/** ディスカバー画面ルート */
@Serializable
object DiscoverRoute

/** マイページ画面ルート */
@Serializable
object MyPageRoute

/** 詳細画面ルート */
@Serializable
data class DetailRoute(val nodeId: String)

// ---------------------------------------------------------------------------
// タブ定義
// ---------------------------------------------------------------------------

/** ボトムナビゲーションのタブアイテム */
private enum class BottomTab(
    val label: String,
    val icon: @Composable () -> Unit,
    val route: String,
) {
    Home(
        label = "ホーム",
        icon = { Icon(Icons.Default.Home, contentDescription = "ホーム") },
        route = "home",
    ),
    Discover(
        label = "ディスカバー",
        icon = { Icon(Icons.Default.Search, contentDescription = "ディスカバー") },
        route = "discover",
    ),
    MyPage(
        label = "マイページ",
        icon = { Icon(Icons.Default.Person, contentDescription = "マイページ") },
        route = "mypage",
    ),
}

// ---------------------------------------------------------------------------
// MainScreen
// ---------------------------------------------------------------------------

/**
 * アプリのメイン画面。
 *
 * BottomNavigation（3タブ）+ FAB + NavHostを持つ基盤画面。
 * FABはホームとディスカバータブでのみ表示される。
 * FABタップでPostTypeSelectSheetを表示し、課題/アイデア投稿画面に遷移する。
 */
@Composable
fun MainScreen(
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    /** 投稿タイプ選択シート表示フラグ */
    var showPostTypeSheet by remember { mutableStateOf(false) }

    /** 課題投稿画面表示フラグ */
    var showIssuePost by remember { mutableStateOf(false) }

    /** アイデア投稿画面表示フラグ */
    var showIdeaPost by remember { mutableStateOf(false) }

    /** FABを表示するタブかどうか */
    val isFabVisible = currentRoute in listOf(BottomTab.Home.route, BottomTab.Discover.route)

    Scaffold(
        modifier = modifier,
        bottomBar = {
            MainBottomBar(
                navController = navController,
                currentRoute = currentRoute,
            )
        },
        floatingActionButton = {
            if (isFabVisible) {
                FloatingActionButton(
                    onClick = { showPostTypeSheet = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(Icons.Default.Add, contentDescription = "投稿")
                }
            }
        },
    ) { innerPadding ->
        // 投稿タイプ選択シート
        if (showPostTypeSheet) {
            PostTypeSelectSheet(
                onDismiss = { showPostTypeSheet = false },
                onIssueSelected = {
                    showPostTypeSheet = false
                    showIssuePost = true
                },
                onIdeaSelected = {
                    showPostTypeSheet = false
                    showIdeaPost = true
                },
            )
        }

        // 課題投稿画面（フルスクリーンダイアログ）
        if (showIssuePost) {
            Dialog(
                onDismissRequest = { showIssuePost = false },
                properties = DialogProperties(usePlatformDefaultWidth = false),
            ) {
                IssuePostScreen(
                    onDismiss = { showIssuePost = false },
                )
            }
        }

        // アイデア投稿画面（フルスクリーンダイアログ）
        if (showIdeaPost) {
            Dialog(
                onDismissRequest = { showIdeaPost = false },
                properties = DialogProperties(usePlatformDefaultWidth = false),
            ) {
                IdeaPostScreen(
                    onDismiss = { showIdeaPost = false },
                )
            }
        }

        NavHost(
            navController = navController,
            startDestination = BottomTab.Home.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(BottomTab.Home.route) {
                HomeScreenPlaceholder()
            }
            composable(BottomTab.Discover.route) {
                DiscoverScreenPlaceholder()
            }
            composable(BottomTab.MyPage.route) {
                MyPageScreenPlaceholder(
                    onLogout = { authViewModel.logout() },
                )
            }
            composable(
                route = "detail/{nodeId}",
                arguments = listOf(navArgument("nodeId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val nodeId = backStackEntry.arguments?.getString("nodeId") ?: return@composable
                DetailScreen(
                    nodeId = nodeId,
                    onNodeClick = { childId ->
                        navController.navigate("detail/$childId")
                    },
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// BottomBar
// ---------------------------------------------------------------------------

/**
 * メイン画面のボトムナビゲーションバー。
 */
@Composable
private fun MainBottomBar(
    navController: NavController,
    currentRoute: String?,
) {
    NavigationBar {
        BottomTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = currentRoute == tab.route,
                onClick = {
                    navController.navigate(tab.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = tab.icon,
                label = { Text(tab.label) },
            )
        }
    }
}

// ---------------------------------------------------------------------------
// プレースホルダー画面（後続PRで差し替え）
// ---------------------------------------------------------------------------

/**
 * ホーム画面のプレースホルダー。
 *
 * - Note: 後続PRで HomeScreen に差し替える
 */
@Composable
fun HomeScreenPlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "ホーム（実装中）",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * ディスカバー画面のプレースホルダー。
 *
 * - Note: 後続PRで DiscoverScreen に差し替える
 */
@Composable
fun DiscoverScreenPlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "ディスカバー（実装中）",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * マイページ画面のプレースホルダー。
 *
 * - Note: 後続PRで MyPageScreen に差し替える
 */
@Composable
fun MyPageScreenPlaceholder(onLogout: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "マイページ（実装中）",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
