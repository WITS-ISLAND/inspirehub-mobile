#!/bin/sh
set -e

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
    echo "Error: DISCORD_BOT_TOKEN is not set. Set it in Xcode Cloud environment variables."
    exit 1
fi

if [ -z "$DISCORD_CHANNEL_ID" ]; then
    echo "Error: DISCORD_CHANNEL_ID is not set. Set it in Xcode Cloud environment variables."
    exit 1
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
SHORT_COMMIT=$(echo "${CI_COMMIT:-unknown}" | cut -c1-7)

# --- ビルド結果を判定 ---
if [ "$CI_XCODEBUILD_EXIT_CODE" = "0" ]; then
    TITLE="🎉 ビルド成功！ InspireHub Mobile"
    DESCRIPTION="いい感じにビルド通ったで！テスト配信の準備OKや 🚀"
    COLOR=3066993
    THUMBNAIL="https://cdn.discordapp.com/emojis/1080553753157832724.webp"
else
    TITLE="😱 ビルド失敗... InspireHub Mobile"
    DESCRIPTION="あかん、ビルドコケた... exit code: ${CI_XCODEBUILD_EXIT_CODE} 🔥"
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
      { "name": "🌿 Branch", "value": "\`${CI_BRANCH:-unknown}\`", "inline": true },
      { "name": "🔢 Build", "value": "#${CI_BUILD_NUMBER:-N/A}", "inline": true },
      { "name": "🛠 Xcode", "value": "${CI_XCODE_VERSION:-unknown}", "inline": true },
      { "name": "📝 Commit", "value": "\`${SHORT_COMMIT}\`", "inline": true },
      { "name": "⚙️ Workflow", "value": "${CI_WORKFLOW:-default}", "inline": true },
      { "name": "📋 直近の変更", "value": "${COMMITS_ESCAPED}", "inline": false }
    ],
    "footer": { "text": "Xcode Cloud ☁️ | InspireHub Mobile" },
    "timestamp": "$(date -u +%Y-%m-%dT%H:%M:%SZ)"
  }]
}
ENDJSON
)

curl -f -X POST \
    "https://discord.com/api/v10/channels/${DISCORD_CHANNEL_ID}/messages" \
    -H "Authorization: Bot ${DISCORD_BOT_TOKEN}" \
    -H "Content-Type: application/json" \
    -d "$PAYLOAD"

echo ""
echo "=== Discord notification sent ==="
