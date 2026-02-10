---
name: implement
description: GitHub IssueからPlan作成→承認→ブランチ作成→実装→PR→TestFlight配信タグまでの一連フローを実行する
user-invocable: true
argument-hint: "(Issueの内容に従って実装を進める)"
allowed-tools: Read, Write, Edit, Grep, Glob, Bash, WebSearch, WebFetch
---

# /implement — Issue駆動実装スキル

GitHub IssueからClaude Codeが実装を行う際の標準フロー。

## フロー

### 1. Plan作成

- Issueの内容を分析し、`.claude/plans/issue-{number}.md` にPlanファイルを作成
- Plan内容:
  - 変更対象ファイル一覧
  - 実装方針
  - 影響範囲
  - テスト方針

### 2. 承認待ち

- PlanをIssueコメントとして投稿する
- **承認前に実装を開始しない**
- ユーザーから承認コメント（「OK」「LGTM」「進めて」等）があるまで待機

### 3. ブランチ作成

- `claude/issue-{number}` ブランチを作成して実装開始
- 1 Issue = 1 ブランチ
- やり直しの場合はブランチを削除して同名で再作成

### 4. 実装

- CLAUDE.mdおよび `.claude/rules/` のルールに従って実装
- コミットメッセージはConventional Commits準拠
- コミットメッセージに `Closes #{issue番号}` を含める

### 5. shared層テスト

- `./gradlew :shared:testDebugUnitTest` を実行
- テストが通ることを確認してからPR作成に進む

### 6. PR作成

- PRを作成
- 本文に `Closes #{issue番号}` を含める
- 変更内容のSummaryとTest planを記載

### 7. タグpush

- PR作成後、`dev/pr-{PR番号}` タグをpushする
- このタグがXcode Cloud Dev版ビルド＆TestFlight配信のトリガーになる

### 8. Planファイル削除

- `.claude/plans/issue-{number}.md` を削除してコミット

## 注意事項

- **iOSビルドは実行しない**（GitHub Actions上のLinuxで動作するため）
- iOSの動作確認はXcode Cloud → TestFlight配信後に人間が行う
- shared層の変更がなくてもタグpushは必ず行う（iOSへの影響確認のため）
