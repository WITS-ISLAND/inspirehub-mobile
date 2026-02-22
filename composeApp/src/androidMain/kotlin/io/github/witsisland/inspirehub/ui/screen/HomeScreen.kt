package io.github.witsisland.inspirehub.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.witsisland.inspirehub.domain.store.HomeTab
import io.github.witsisland.inspirehub.domain.store.SortOrder
import io.github.witsisland.inspirehub.presentation.viewmodel.HomeViewModel
import io.github.witsisland.inspirehub.ui.components.NodeCard
import org.koin.compose.viewmodel.koinViewModel

/**
 * ホーム画面
 *
 * ノード（課題・アイデア）のフィードをタブ切替・ソート・プルリフレッシュで閲覧できる画面。
 * タブ: 全て / 課題 / アイデア / 自分
 * ソート: 新着順 / 人気順
 *
 * - Note: [onNodeClick] でノードIDを受け取り、詳細画面へ遷移する
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNodeClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val nodes by viewModel.nodes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()

    /** ソートメニューの表示状態 */
    var showSortMenu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.onAppear()
    }

    Column(modifier = modifier.fillMaxSize()) {
        // TopAppBar（タイトル + ソートメニュー）
        TopAppBar(
            title = {
                Text(
                    text = "InspireHub",
                    style = MaterialTheme.typography.titleLarge,
                )
            },
            actions = {
                Box {
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.SwapVert,
                            contentDescription = "ソート",
                        )
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false },
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "新着順",
                                    color = if (sortOrder == SortOrder.RECENT) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                )
                            },
                            onClick = {
                                viewModel.setSortOrder(SortOrder.RECENT)
                                showSortMenu = false
                            },
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "人気順",
                                    color = if (sortOrder == SortOrder.POPULAR) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                )
                            },
                            onClick = {
                                viewModel.setSortOrder(SortOrder.POPULAR)
                                showSortMenu = false
                            },
                        )
                    }
                }
            },
        )

        // タブ（新着/課題/アイデア/自分）
        HomeTabRow(
            currentTab = currentTab,
            onTabSelected = { viewModel.setTab(it) },
        )

        // ローディングインジケーター（タブの直下）
        if (isLoading && nodes.isEmpty()) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        // コンテンツ
        PullToRefreshBox(
            isRefreshing = isLoading && nodes.isNotEmpty(),
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.fillMaxSize(),
        ) {
            when {
                error != null && nodes.isEmpty() -> {
                    // エラー状態（ノードがない場合）
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = error ?: "読み込みに失敗しました",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                        Button(
                            onClick = { viewModel.refresh() },
                            modifier = Modifier.padding(top = 16.dp),
                        ) {
                            Text("再読み込み")
                        }
                    }
                }
                !isLoading && nodes.isEmpty() -> {
                    // 空状態
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "まだ投稿がありません",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                else -> {
                    // ノード一覧
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(nodes, key = { it.id }) { node ->
                            NodeCard(
                                node = node,
                                onClick = { onNodeClick(node.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * ホーム画面のタブ行コンポーネント
 *
 * 全て / 課題 / アイデア / 自分 の4タブを横スクロールで表示する。
 */
@Composable
private fun HomeTabRow(
    currentTab: HomeTab,
    onTabSelected: (HomeTab) -> Unit,
) {
    /** タブの表示定義リスト */
    val tabs = listOf(
        HomeTabItem(tab = HomeTab.ALL, label = "全て"),
        HomeTabItem(tab = HomeTab.ISSUES, label = "課題"),
        HomeTabItem(tab = HomeTab.IDEAS, label = "アイデア"),
        HomeTabItem(tab = HomeTab.MINE, label = "自分"),
    )
    val selectedIndex = tabs.indexOfFirst { it.tab == currentTab }.coerceAtLeast(0)

    ScrollableTabRow(
        selectedTabIndex = selectedIndex,
        edgePadding = 0.dp,
    ) {
        tabs.forEachIndexed { index, item ->
            Tab(
                selected = index == selectedIndex,
                onClick = { onTabSelected(item.tab) },
                text = { Text(item.label) },
            )
        }
    }
}

/**
 * ホームタブの表示定義
 *
 * @property tab 対応する [HomeTab] の値
 * @property label 表示ラベル文字列
 */
private data class HomeTabItem(
    val tab: HomeTab,
    val label: String,
)
