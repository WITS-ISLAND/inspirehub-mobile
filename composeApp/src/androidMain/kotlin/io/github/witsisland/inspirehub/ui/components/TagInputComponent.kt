package io.github.witsisland.inspirehub.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.witsisland.inspirehub.domain.model.Tag

/** タグ上限数 */
private const val MAX_TAGS = 5

/**
 * タグ入力コンポーネント
 *
 * タグの入力・サジェスト表示・追加済みタグ一覧を提供する。
 * タグは最大5個まで追加可能。
 *
 * - Note: onSearchSuggestions はユーザー入力のたびに呼ばれる。デバウンスはViewModel側で行う。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagInputComponent(
    /** 追加済みタグ一覧 */
    tags: List<String>,
    /** APIサジェスト候補一覧 */
    tagSuggestions: List<Tag>,
    /** タグ追加コールバック */
    onAddTag: (String) -> Unit,
    /** タグ削除コールバック */
    onRemoveTag: (String) -> Unit,
    /** サジェスト検索コールバック */
    onSearchSuggestions: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var inputText by remember { mutableStateOf("") }
    val isMaxTagsReached = tags.size >= MAX_TAGS

    Column(modifier = modifier.fillMaxWidth()) {
        // タグ入力フィールド
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { newValue ->
                    inputText = newValue
                    onSearchSuggestions(newValue)
                },
                placeholder = { Text("タグを入力...") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                enabled = !isMaxTagsReached,
                label = { Text("タグ（任意・最大${MAX_TAGS}個）") },
            )
            IconButton(
                onClick = {
                    val trimmed = inputText.trim()
                    if (trimmed.isNotEmpty()) {
                        onAddTag(trimmed)
                        inputText = ""
                        onSearchSuggestions("")
                    }
                },
                enabled = inputText.trim().isNotEmpty() && !isMaxTagsReached,
            ) {
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = "タグを追加",
                    tint = if (inputText.trim().isNotEmpty() && !isMaxTagsReached) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    },
                    modifier = Modifier.size(28.dp),
                )
            }
        }

        // サジェスト候補（横スクロール）
        if (tagSuggestions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(tagSuggestions, key = { it.id }) { tag ->
                    FilterChip(
                        selected = false,
                        onClick = {
                            onAddTag(tag.name)
                            inputText = ""
                            onSearchSuggestions("")
                        },
                        label = { Text("#${tag.name}") },
                        enabled = !isMaxTagsReached,
                    )
                }
            }
        }

        // 追加済みタグ（FlowRow）
        if (tags.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                tags.forEach { tag ->
                    InputChip(
                        selected = false,
                        onClick = { },
                        label = { Text("#$tag") },
                        trailingIcon = {
                            IconButton(
                                onClick = { onRemoveTag(tag) },
                                modifier = Modifier.size(18.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "タグ「$tag」を削除",
                                    modifier = Modifier.size(14.dp),
                                )
                            }
                        },
                    )
                }
            }
        }
    }
}
