# チーム開発ワークフロールール

## Git Worktree によるエージェント分離（必須）

複数エージェントを同時にspawnする場合、**各エージェントは専用のgit worktreeで作業**すること。

### ルール

1. **エージェントspawn前にworktreeを作成**
   ```bash
   git worktree add ~/.claude-worktrees/inspirehub-mobile/<branch-name> -b <branch-name>
   ```

2. **ブランチ命名規則**: `feat/<stream>-<概要>` or `test/<stream>-<概要>`
   - 例: `feat/stream-a-domain-model`, `test/stream-c-viewmodel-tests`, `feat/stream-d-ios-ui`

3. **worktreeパス**: `~/.claude-worktrees/inspirehub-mobile/<branch-name>`

4. **エージェントへの指示にworktreeパスを含める**
   - 作業ディレクトリとして worktree パスを明示

5. **マージ戦略**:
   - 依存関係のあるストリームは、先行ストリームのブランチからworktreeを作成
   - 完了後は main にマージ（team-leadまたはユーザーが実施）
   - マージ順序は依存グラフに従う

### 例: 3エージェント体制

```bash
# kotlin-dev-1: shared層の基盤開発
git worktree add ~/.claude-worktrees/inspirehub-mobile/feat/stream-a-shared -b feat/stream-a-shared

# kotlin-dev-2: テスト開発
git worktree add ~/.claude-worktrees/inspirehub-mobile/test/stream-c-tests -b test/stream-c-tests

# ios-dev-1: iOS UI開発（shared層の成果物に依存する場合、stream-aブランチから派生）
git worktree add ~/.claude-worktrees/inspirehub-mobile/feat/stream-d-ios-ui -b feat/stream-d-ios-ui feat/stream-a-shared
```

### エージェントpromptテンプレ

```
作業ディレクトリ: ~/.claude-worktrees/inspirehub-mobile/<branch-name>
ブランチ: <branch-name>
コミットはこのworktree内で行え。mainには直接pushするな。
```

## PR作成前のビルドチェック（必須）

PRを作成する前に、以下のビルドチェックを**必ず**全て通過させること。

### チェック項目

```bash
# 1. shared層テスト
./gradlew :shared:testDebugUnitTest

# 2. Androidビルド
./gradlew :composeApp:assembleDebug

# 3. iOSビルド
cd iosApp && xcodebuild -project iosApp.xcodeproj -scheme iosApp \
  -configuration Debug -destination 'generic/platform=iOS Simulator' -quiet
```

### ルール

1. **全ビルドが通過するまでPRを作成しない**
2. ビルドエラーがある場合は修正してからPR作成
3. 既存のビルドエラー（PR変更とは無関係）がある場合は、PR本文にその旨を明記
4. `/build-check` スキルを活用してもよい

### 実施方法

- チームリードが直接ビルドチェックを行わず、**qa-checker エージェントに委任**する
- qa-checker がビルドエラーを発見した場合は修正コミットを追加してpush

## ユーザー動作確認用コマンド（必須表示）

worktreeでiOS作業を行った場合、**PR作成時にユーザーへ以下のコマンドを必ず表示**すること：

```bash
# Xcodeでworktreeのプロジェクトを開く
open ~/.claude-worktrees/inspirehub-mobile/<branch-name>/iosApp/iosApp.xcodeproj
```

ユーザーがworktreeの場所を探す手間を省くため、毎回明示すること。

## デザインレビュー（PR前の最終チェック）

iOS UIの変更を含むPRを作成する前に、**design-reviewer エージェントによるチェック**を実施すること。

### 手順

1. UIの実装が完了しビルド成功を確認
2. design-reviewer エージェントをspawnしてレビュー依頼
3. レビュー指摘があれば修正
4. 修正完了後にPR作成

### design-reviewerの確認項目

- 画面設計書（`docs/design/画面設計_ネイティブアプリ.md`）との一致
- SwiftUIデザインガイド準拠
- iOS HIG（Human Interface Guidelines）準拠
- アクセシビリティ対応

## コンフリクト防止

- 同じファイルを複数エージェントが同時に編集することを避ける
- 依存関係のあるタスクは順序を守る（blockedBy）
- shared層の変更がiOS/Android層に影響する場合、shared側が先にマージされてからUI側を開始
