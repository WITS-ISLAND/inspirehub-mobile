package io.github.witsisland.inspirehub.ui.screen

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.witsisland.inspirehub.domain.model.Node
import io.github.witsisland.inspirehub.domain.model.NodeType
import io.github.witsisland.inspirehub.domain.model.Tag
import io.github.witsisland.inspirehub.presentation.viewmodel.DiscoverViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * ディスカバー画面
 *
 * 人気タグの横スクロールフィルターと人気投稿一覧、キーワード検索を提供する。
 * タグ選択時はそのタグに紐づいたノードを表示し、検索クエリ入力時は検索結果を表示する。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(
    onNodeClick: (String) -> Unit,
    viewModel: DiscoverViewModel = koinViewModel(),
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val popularTags by viewModel.popularTags.collectAsState()
    val popularNodes by viewModel.popularNodes.collectAsState()
    val selectedTag by viewModel.selectedTag.collectAsState()
    val tagNodes by viewModel.tagNodes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadPopularTags()
        viewModel.loadPopularNodes()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ディスカバー") },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // 検索バー
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.search(it) },
                    placeholder = { Text("キーワードで検索...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }

            // 人気のタグセクション
            if (popularTags.isNotEmpty()) {
                item {
                    PopularTagsSection(
                        tags = popularTags,
                        selectedTag = selectedTag,
                        onTagClick = { viewModel.selectTag(it) },
                    )
                }
            }

            // ローディング
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                    }
                }
            }

            // エラー
            if (error != null) {
                item {
                    Text(
                        text = error ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            // 検索結果またはタグ絞り込み結果
            val displayNodes = when {
                searchQuery.isNotBlank() -> searchResults
                selectedTag != null -> tagNodes
                else -> null
            }

            if (displayNodes != null) {
                if (displayNodes.isEmpty() && !isLoading) {
                    item {
                        Text(
                            text = "該当する投稿はありません",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    item {
                        Text(
                            text = if (selectedTag != null) "#${selectedTag!!.name}" else "検索結果",
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                    items(displayNodes, key = { it.id }) { node ->
                        NodeListItem(node = node, onClick = { onNodeClick(node.id) })
                    }
                }
            } else {
                // 人気の投稿セクション
                item {
                    PopularNodesSection(
                        nodes = popularNodes,
                        onNodeClick = onNodeClick,
                    )
                }
            }
        }
    }
}

/**
 * 人気タグの横スクロールセクション
 */
@Composable
private fun PopularTagsSection(
    tags: List<Tag>,
    selectedTag: Tag?,
    onTagClick: (Tag) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "人気のタグ",
            style = MaterialTheme.typography.titleMedium,
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(tags, key = { it.id }) { tag ->
                val isSelected = selectedTag?.id == tag.id
                FilterChip(
                    selected = isSelected,
                    onClick = { onTagClick(tag) },
                    label = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text("#${tag.name}")
                            if (tag.usageCount > 0) {
                                Text(
                                    text = "${tag.usageCount}",
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                )
            }
        }
    }
}

/**
 * 人気の投稿セクション
 */
@Composable
private fun PopularNodesSection(
    nodes: List<Node>,
    onNodeClick: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "人気の投稿",
            style = MaterialTheme.typography.titleMedium,
        )
        if (nodes.isEmpty()) {
            Text(
                text = "投稿はまだありません",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                nodes.forEach { node ->
                    NodeListItem(node = node, onClick = { onNodeClick(node.id) })
                    HorizontalDivider()
                }
            }
        }
    }
}

/**
 * ノードリストアイテム
 *
 * DiscoverScreen・MyPageScreenで共用するシンプルなノード表示コンポーネント。
 * ノードタイプアイコン・タイトル・chevronを横並びで表示する。
 */
@Composable
internal fun NodeListItem(
    node: Node,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 0.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = when (node.type) {
                    NodeType.ISSUE -> Icons.Default.Warning
                    NodeType.IDEA -> Icons.Default.Lightbulb
                    NodeType.PROJECT -> Icons.Default.Lightbulb
                },
                contentDescription = when (node.type) {
                    NodeType.ISSUE -> "課題"
                    NodeType.IDEA -> "アイデア"
                    NodeType.PROJECT -> "プロジェクト"
                },
                tint = when (node.type) {
                    NodeType.ISSUE -> MaterialTheme.colorScheme.error
                    NodeType.IDEA -> MaterialTheme.colorScheme.primary
                    NodeType.PROJECT -> MaterialTheme.colorScheme.secondary
                },
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = node.title,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (node.authorName.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = node.authorName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}
