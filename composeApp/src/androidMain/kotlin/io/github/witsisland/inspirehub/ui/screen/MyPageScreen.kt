package io.github.witsisland.inspirehub.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.witsisland.inspirehub.presentation.viewmodel.MyPageViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * マイページ画面
 *
 * ログインユーザーのプロフィール情報、自分の投稿一覧、リアクションした投稿一覧、ログアウトボタンを提供する。
 *
 * - Note: ログアウト時は確認ダイアログを表示し、ユーザーの意図を確認する
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPageScreen(
    onLogout: () -> Unit,
    onNodeClick: (String) -> Unit,
    viewModel: MyPageViewModel = koinViewModel(),
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val myNodes by viewModel.myNodes.collectAsState()
    val reactedNodes by viewModel.reactedNodes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    /** ログアウト確認ダイアログの表示フラグ */
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    // ログアウト確認ダイアログ
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("ログアウト") },
            text = { Text("ログアウトしてもよいですか？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout()
                        onLogout()
                    },
                ) {
                    Text(
                        text = "ログアウト",
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("キャンセル")
                }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("マイページ") },
            )
        },
    ) { innerPadding ->
        if (isLoading && currentUser == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            // プロフィールセクション
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp),
                        )
                    }
                    Text(
                        text = currentUser?.handle ?: "",
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        text = "メンバー",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            item { HorizontalDivider() }

            // エラー表示
            if (error != null) {
                item {
                    Text(
                        text = error ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }

            // 自分の投稿セクションヘッダー
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "自分の投稿",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "${myNodes.size}件",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // 自分の投稿リスト
            if (myNodes.isEmpty()) {
                item {
                    Text(
                        text = "投稿はまだありません",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            } else {
                items(myNodes, key = { it.id }) { node ->
                    Column {
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            NodeListItem(node = node, onClick = { onNodeClick(node.id) })
                        }
                        HorizontalDivider(modifier = Modifier.padding(start = 16.dp))
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
            }

            // リアクションした投稿セクションヘッダー
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "リアクションした投稿",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "${reactedNodes.size}件",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // リアクションした投稿リスト
            if (reactedNodes.isEmpty()) {
                item {
                    Text(
                        text = "リアクションした投稿はまだありません",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            } else {
                items(reactedNodes, key = { "reacted_${it.id}" }) { node ->
                    Column {
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            NodeListItem(node = node, onClick = { onNodeClick(node.id) })
                        }
                        HorizontalDivider(modifier = Modifier.padding(start = 16.dp))
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
            }

            // ログアウトボタン
            item {
                TextButton(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    Text(
                        text = "ログアウト",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}
