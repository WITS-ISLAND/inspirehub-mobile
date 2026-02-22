package io.github.witsisland.inspirehub.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.dp
import io.github.witsisland.inspirehub.presentation.viewmodel.PostViewModel
import io.github.witsisland.inspirehub.ui.components.TagInputComponent
import org.koin.compose.viewmodel.koinViewModel

/**
 * 課題投稿画面
 *
 * タイトル・本文・タグを入力して課題を投稿するフォーム画面。
 * タイトルと本文が入力されないと投稿ボタンが無効になる。
 *
 * - Note: 投稿完了後は onDismiss() を呼んで画面を閉じる
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssuePostScreen(
    /** 画面を閉じるコールバック（完了・キャンセル共通） */
    onDismiss: () -> Unit,
    /** 課題投稿ViewModel */
    viewModel: PostViewModel = koinViewModel(),
) {
    val title by viewModel.title.collectAsState()
    val content by viewModel.content.collectAsState()
    val tags by viewModel.tags.collectAsState()
    val tagSuggestions by viewModel.suggestedTags.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val isValid by viewModel.isValid.collectAsState()
    val isSuccess by viewModel.isSuccess.collectAsState()

    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            viewModel.reset()
            onDismiss()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("課題を投稿") },
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
                        onClick = { viewModel.submitIssue() },
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
                    placeholder = { Text("課題のタイトルを入力...") },
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
                    placeholder = { Text("課題の詳細を入力...") },
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

                Spacer(modifier = Modifier.height(20.dp))

                // テンプレ例文セクション
                Text(
                    text = "テンプレート例文",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "例: 「リモートワークでの非同期コミュニケーションが難しく、情報の抜け漏れが発生している。特にタイムゾーンをまたぐメンバーとの連携で課題を感じている。」",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
