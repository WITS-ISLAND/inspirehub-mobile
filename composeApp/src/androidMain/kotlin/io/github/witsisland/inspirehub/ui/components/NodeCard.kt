package io.github.witsisland.inspirehub.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.witsisland.inspirehub.domain.model.Node
import io.github.witsisland.inspirehub.domain.model.NodeType
import kotlinx.datetime.Instant
import kotlinx.datetime.Clock

/**
 * ノード（課題・アイデア）を表示するカードコンポーネント
 *
 * ホーム画面やリスト画面でノードを一覧表示する際に使用する。
 * ノードタイプバッジ、タイトル、本文プレビュー、タグ、投稿者、リアクション数を表示する。
 *
 * - Note: タップ時は [onClick] が呼ばれる
 */
@Composable
fun NodeCard(
    node: Node,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // ノードタイプバッジ + 相対時間
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                NodeTypeBadge(type = node.type)
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = relativeTime(node.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // タイトル（最大2行）
            Text(
                text = node.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            // 本文プレビュー（最大3行、空の場合は非表示）
            if (node.content.isNotBlank()) {
                Text(
                    text = node.content,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // タグChips（横スクロール、最大3つ + 残数バッジ）
            if (node.tagIds.isNotEmpty()) {
                TagChipsRow(tags = node.tagIds)
            }

            // 投稿者名
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = node.authorName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // リアクション行
            ReactionRow(node = node)

            // 派生元バッジ（parentNode がある場合のみ）
            node.parentNode?.let { parent ->
                ParentNodeBadge(
                    parentTitle = parent.title,
                    parentType = parent.type,
                )
            }
        }
    }
}

/**
 * ノードタイプを示すバッジコンポーネント
 *
 * 課題は赤系、アイデアは青系、プロジェクトは緑系で表示する。
 */
@Composable
private fun NodeTypeBadge(type: NodeType) {
    val (label, backgroundColor, contentColor) = when (type) {
        NodeType.ISSUE -> Triple(
            "課題",
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
        )
        NodeType.IDEA -> Triple(
            "アイデア",
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
        )
        NodeType.PROJECT -> Triple(
            "プロジェクト",
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
        )
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
        )
    }
}

/**
 * タグChipsを横スクロールで表示する行コンポーネント
 *
 * 最大3件を表示し、残りがある場合は「+N」バッジで補足する。
 */
@Composable
private fun TagChipsRow(tags: List<String>) {
    val maxVisible = 3
    val visibleTags = tags.take(maxVisible)
    val remaining = tags.size - visibleTags.size

    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        visibleTags.forEach { tag ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(horizontal = 8.dp, vertical = 2.dp),
            ) {
                Text(
                    text = "#$tag",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }
        if (remaining > 0) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 2.dp),
            ) {
                Text(
                    text = "+$remaining",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * リアクション数を横並びで表示する行コンポーネント
 *
 * カウントが0のリアクションは非表示にする。
 * isReacted=true の場合はプライマリカラーで強調表示する。
 */
@Composable
private fun ReactionRow(node: Node) {
    val reactions = node.reactions
    val hasAnyReaction = reactions.like.count > 0 ||
        reactions.interested.count > 0 ||
        reactions.wantToTry.count > 0 ||
        node.commentCount > 0

    if (!hasAnyReaction) return

    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (reactions.like.count > 0) {
            ReactionChip(
                emoji = "👍",
                count = reactions.like.count,
                isReacted = reactions.like.isReacted,
            )
        }
        if (reactions.interested.count > 0) {
            ReactionChip(
                emoji = "🔥",
                count = reactions.interested.count,
                isReacted = reactions.interested.isReacted,
            )
        }
        if (reactions.wantToTry.count > 0) {
            ReactionChip(
                emoji = "💪",
                count = reactions.wantToTry.count,
                isReacted = reactions.wantToTry.isReacted,
            )
        }
        if (node.commentCount > 0) {
            Text(
                text = "💬 ${node.commentCount}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * 個別リアクションのチップコンポーネント
 *
 * isReacted=true の場合はプライマリカラーで表示する。
 */
@Composable
private fun ReactionChip(emoji: String, count: Int, isReacted: Boolean) {
    Text(
        text = "$emoji $count",
        style = MaterialTheme.typography.labelSmall,
        color = if (isReacted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

/**
 * 派生元ノードを示すバッジコンポーネント
 *
 * parentNode が存在する場合のみ表示される。
 */
@Composable
private fun ParentNodeBadge(parentTitle: String, parentType: NodeType) {
    val typeLabel = when (parentType) {
        NodeType.ISSUE -> "課題"
        NodeType.IDEA -> "アイデア"
        NodeType.PROJECT -> "プロジェクト"
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "↩ $typeLabel: ",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = parentTitle,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * ISO 8601形式の日時文字列を相対時間表示に変換する
 *
 * 例: "2時間前"、"1日前"、"3週間前"
 */
private fun relativeTime(createdAt: String): String {
    return try {
        val instant = Instant.parse(createdAt)
        val now = Clock.System.now()
        val diffMillis = (now - instant).inWholeMilliseconds
        val diffMinutes = diffMillis / 60_000
        val diffHours = diffMinutes / 60
        val diffDays = diffHours / 24
        val diffWeeks = diffDays / 7
        val diffMonths = diffDays / 30
        when {
            diffMinutes < 1 -> "たった今"
            diffMinutes < 60 -> "${diffMinutes}分前"
            diffHours < 24 -> "${diffHours}時間前"
            diffDays < 7 -> "${diffDays}日前"
            diffWeeks < 5 -> "${diffWeeks}週間前"
            else -> "${diffMonths}ヶ月前"
        }
    } catch (_: Exception) {
        createdAt
    }
}
