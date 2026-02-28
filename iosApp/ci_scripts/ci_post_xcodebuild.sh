#!/bin/sh
# set -eしない: 失敗時もDiscord通知を最後まで実行するため

echo "=== ci_post_xcodebuild.sh ==="

# archiveアクション時のみ実行
if [ "$CI_XCODEBUILD_ACTION" != "archive" ]; then
    echo "Skipping: not an archive action (action=$CI_XCODEBUILD_ACTION)"
    exit 0
fi

# --- Discord設定 ---
# Xcode Cloud の Environment Variables で以下を設定すること:
#   DISCORD_BOT_TOKEN: Discord BotのToken
#   DISCORD_CLIENT_ID: Discord BotのClient ID
#   DISCORD_CHANNEL_ID: 通知先のChannel ID

if [ -z "$DISCORD_BOT_TOKEN" ]; then
    echo "Warning: DISCORD_BOT_TOKEN is not set. Skipping Discord notification."
    exit 0
fi

if [ -z "$DISCORD_CHANNEL_ID" ]; then
    echo "Warning: DISCORD_CHANNEL_ID is not set. Skipping Discord notification."
    exit 0
fi

# --- 直近のコミットメッセージを取得 ---
RECENT_COMMITS=""
if command -v git >/dev/null 2>&1; then
    RECENT_COMMITS=$(cd "$CI_PRIMARY_REPOSITORY_PATH" && git log --oneline -5 2>/dev/null | while read -r line; do
        echo "• ${line}"
    done)
fi
if [ -z "$RECENT_COMMITS" ]; then
    RECENT_COMMITS="(取得できませんでした)"
fi

# --- JSONエスケープ ---
escape_json() {
    printf '%s' "$1" | sed 's/\\/\\\\/g; s/"/\\"/g; s/	/\\t/g' | tr '\n' '\n' | sed ':a;N;$!ba;s/\n/\\n/g'
}

COMMITS_ESCAPED=$(escape_json "$RECENT_COMMITS")

# ブランチ情報を取得（CI_GIT_REFの方がより正確）
BRANCH_NAME="${CI_GIT_REF:-${CI_BRANCH:-unknown}}"
# refs/heads/ プレフィックスを削除
BRANCH_NAME=$(echo "$BRANCH_NAME" | sed 's|^refs/heads/||')

# mainブランチかどうか判定（外部テスター配信対象か）
IS_MAIN_BRANCH=false
if [ "$BRANCH_NAME" = "main" ]; then
    IS_MAIN_BRANCH=true
fi

# --- ビルド結果を判定 ---
if [ "$CI_XCODEBUILD_EXIT_CODE" = "0" ]; then
    if [ "$IS_MAIN_BRANCH" = "true" ]; then
        TITLE="🚀 本番ビルド成功！ InspireHub Mobile"
        DESCRIPTION="mainブランチのビルドが通ったで！**外部テスターへの配信を開始**するで 👥✨\nTestFlightの外部テスターグループへ自動配信中..."
    else
        TITLE="🎉 ビルド成功！ InspireHub Mobile"
        DESCRIPTION="いい感じにビルド通ったで！テスト配信の準備OKや 🚀"
    fi
    COLOR=3066993
    THUMBNAIL="https://cdn.discordapp.com/emojis/1080553753157832724.webp"
else
    if [ "$IS_MAIN_BRANCH" = "true" ]; then
        TITLE="😱 本番ビルド失敗... InspireHub Mobile"
        DESCRIPTION="あかん、mainブランチのビルドがコケた！外部テスターへの配信ができへん... exit code: ${CI_XCODEBUILD_EXIT_CODE} 🔥\n早めに修正して再pushしてな！"
    else
        TITLE="😱 ビルド失敗... InspireHub Mobile"
        DESCRIPTION="あかん、ビルドコケた... exit code: ${CI_XCODEBUILD_EXIT_CODE} 🔥"
    fi
    COLOR=15158332
    THUMBNAIL=""
fi

# --- Discord Bot APIで通知 ---
echo "Sending build notification to Discord (channel=$DISCORD_CHANNEL_ID)..."

PAYLOAD=$(cat <<ENDJSON
{
  "embeds": [{
    "title": "${TITLE}",
    "description": "${DESCRIPTION}",
    "color": ${COLOR},
    "fields": [
      { "name": "⚙️ Workflow", "value": "${CI_WORKFLOW:-default}", "inline": true },
      { "name": "🌿 Branch", "value": "\`${BRANCH_NAME}\`", "inline": true },
      { "name": "🔢 Build", "value": "#${CI_BUILD_NUMBER:-N/A}", "inline": true },
      { "name": "📋 直近の変更", "value": "${COMMITS_ESCAPED}", "inline": false }
    ],
    "footer": { "text": "Xcode Cloud ☁️ | InspireHub Mobile" },
    "timestamp": "$(date -u +%Y-%m-%dT%H:%M:%SZ)"
  }]
}
ENDJSON
)

HTTP_CODE=$(curl -s -o /tmp/discord_response.txt -w "%{http_code}" -X POST \
    "https://discord.com/api/v10/channels/${DISCORD_CHANNEL_ID}/messages" \
    -H "Authorization: Bot ${DISCORD_BOT_TOKEN}" \
    -H "Content-Type: application/json" \
    -d "$PAYLOAD" 2>&1) || true

if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "204" ]; then
    echo "Discord notification sent successfully (HTTP $HTTP_CODE)"
else
    echo "Warning: Discord notification failed (HTTP $HTTP_CODE)"
    cat /tmp/discord_response.txt 2>/dev/null || true
fi

echo ""
echo "=== ci_post_xcodebuild.sh done ==="
