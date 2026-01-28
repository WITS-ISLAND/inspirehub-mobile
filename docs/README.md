# InspireHub Mobile ドキュメント

## 概要

このディレクトリにはInspireHub Mobileの設計ドキュメントが含まれています。

## ドキュメント構成

### design/
アプリケーションの設計ドキュメント

- `persona.md` - ペルソナ定義
- `journey-map.md` - ユーザージャーニーマップ
- `object-model.md` - オブジェクトモデル図
- `feature-list.md` - 機能一覧
- `screen-design.md` - 画面設計

## Google Driveとの同期

**マスターソース**: [Google Drive - 02_設計書](https://drive.google.com/drive/folders/1iWzHvA2qXEuSq15yMt-14Is5N13FWGkW?usp=sharing)

### 同期手順

1. **Serverチームが設計を更新した場合**:
   - Slack/Discord等で通知
   - Mobileチームが該当ファイルをダウンロード
   - `docs/design/` 内のファイルを更新
   - Gitコミット

2. **Mobileチームが設計を更新する場合**:
   - `docs/design/` 内のファイルを編集
   - Gitコミット
   - Google Driveへアップロード（上書き）
   - Serverチームに通知

### 注意事項

- Git内のMarkdownファイルが**Single Source of Truth**です
- Google Driveはバックアップ・共有用
- コンフリクトが発生した場合は、チーム内で調整してください
