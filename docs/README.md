# InspireHub Mobile ドキュメント

## ドキュメント構成

### design/ — 設計ドキュメント

| ファイル | 内容 |
| ------- | ---- |
| ペルソナ.md | ユーザーペルソナ定義 |
| ジャーニーマップ.html | ユーザージャーニーマップ |
| ドメインモデル図.md | ドメインモデル図 |
| 機能一覧.md | 機能リストとフェーズ計画（Phase 1-3） |
| 画面設計_ネイティブアプリ.md | 画面設計仕様 |
| swiftui_design_guide.md | SwiftUIデザインガイド（色、タイポグラフィ、スペーシング） |
| link_expression_proposals.md | リンク表現提案 |
| spm_multimodule_architecture.md | SPMマルチモジュール化の設計 |

### ルートレベル

| ファイル | 内容 |
| ------- | ---- |
| architecture.md | アーキテクチャ概要（MVVM + Store Pattern） |
| qa_checklist.md | Phase 1 QAチェックリスト |
| ci_cd_strategy.md | CI/CD戦略 |
| spm_xcodecloud_report.md | SPM + Xcode Cloud検証レポート |

## Google Driveとの同期

**マスターソース**: [Google Drive - 02_設計書](https://drive.google.com/drive/folders/1iWzHvA2qXEuSq15yMt-14Is5N13FWGkW?usp=sharing)

### 同期手順

1. **Serverチームが設計を更新した場合**:
   - Slack等で通知 → `docs/design/` 内のファイルを更新 → Gitコミット

2. **Mobileチームが設計を更新する場合**:
   - `docs/design/` 内のファイルを編集 → Gitコミット → Google Driveへアップロード → Serverチームに通知

### 注意事項

- Git内のMarkdownファイルが **Single Source of Truth**
- Google Driveはバックアップ・共有用
