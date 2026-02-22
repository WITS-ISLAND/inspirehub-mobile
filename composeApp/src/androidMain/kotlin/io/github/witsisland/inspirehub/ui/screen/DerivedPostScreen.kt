package io.github.witsisland.inspirehub.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.witsisland.inspirehub.domain.model.Node
import io.github.witsisland.inspirehub.domain.model.NodeType
import io.github.witsisland.inspirehub.presentation.viewmodel.PostViewModel
import io.github.witsisland.inspirehub.ui.components.TagInputComponent
import org.koin.compose.viewmodel.koinViewModel

/**
 * 派生投稿画面
 *
 * 既存ノードを派生元として引用し、新しいアイデアを投稿するフォーム画面。
 * 派生元のタイトルを読み取り専用で表示する。
 *
 * - Note: 投稿完了後は onDismiss() を呼んで画面を閉じる
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DerivedPostScreen(
    /** 派生元ノードのID */
    parentNodeId: String,
    /** 派生元ノードのタイトル */
    parentNodeTitle: String,
    /** 派生元ノードの種別（表示用アイコン切り替えに使用） */
    parentNodeType: NodeType = NodeType.IDEA,
    /** 画面を閉じるコールバック（完了・キャンセル共通） */
    onDismiss: () -> Unit,
    /** 派生投稿ViewModel */
    viewModel: PostViewModel = koinViewModel(),
) {
    val title by viewModel.title.collectAsState()
    val content by viewModel.content.collectAsState()
    val tags by viewModel.tags.collectAsState()
    val tagSuggestions by viewModel.suggestedTags.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val isValid by viewModel.isValid.collectAsState()
    val isSuccess by viewModel.isSuccess.collectAsState()

    // 派生元ノードをViewModelにセット（最低限のフィールドで仮Nodeを生成）
    LaunchedEffect(parentNodeId) {
        val stubParentNode = Node(
            id = parentNodeId,
            type = parentNodeType,
            title = parentNodeTitle,
            content = "",
            authorId = "",
            authorName = "",
            createdAt = "",
        )
        viewModel.setParentNode(stubParentNode)
    }

    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            viewModel.reset()
            onDismiss()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("派生投稿") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.reset()
                        onDismiss()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "キャンセル")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.submitDerived() },
                        enabled = isValid && !isSubmitting,
                    ) {
                        Text(
                            text = "投稿",
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            ) {
                // 派生元セクション
                Text(
                    text = "派生元",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                ParentNodeCard(
                    nodeTitle = parentNodeTitle,
                    nodeType = parentNodeType,
                )

                Spacer(modifier = Modifier.height(20.dp))

                // タイトル入力
                Text(
                    text = "タイトル",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { viewModel.updateTitle(it) },
                    placeholder = { Text("派生アイデアのタイトルを入力...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 本文入力
                Text(
                    text = "本文",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = content,
                    onValueChange = { viewModel.updateContent(it) },
                    placeholder = { Text("派生アイデアの詳細を入力...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 150.dp),
                    singleLine = false,
                    maxLines = 10,
                )

                Spacer(modifier = Modifier.height(20.dp))

                // タグ入力
                TagInputComponent(
                    tags = tags,
                    tagSuggestions = tagSuggestions,
                    onAddTag = { viewModel.addTag(it) },
                    onRemoveTag = { viewModel.removeTag(it) },
                    onSearchSuggestions = { viewModel.searchTagSuggestions(it) },
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // 送信中オーバーレイ
            if (isSubmitting) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

/**
 * 派生元ノードを表示するカード
 *
 * 読み取り専用で派生元のノード種別アイコンとタイトルを表示する。
 */
@Composable
private fun ParentNodeCard(
    /** 派生元ノードのタイトル */
    nodeTitle: String,
    /** 派生元ノードの種別 */
    nodeType: NodeType,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.Bolt,
            contentDescription = null,
            tint = if (nodeType == NodeType.ISSUE) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.primary
            },
            modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (nodeType == NodeType.ISSUE) "課題" else "アイデア",
                style = MaterialTheme.typography.labelSmall,
                color = if (nodeType == NodeType.ISSUE) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                },
            )
            Text(
                text = nodeTitle,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
