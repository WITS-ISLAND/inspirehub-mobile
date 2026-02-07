package io.github.witsisland.inspirehub.domain.repository

import io.github.witsisland.inspirehub.domain.model.User

/**
 * 認証リポジトリ
 */
interface AuthRepository {
    /**
     * Google ID Tokenを検証してログイン（SDK方式）
     * @param idToken Google ID Token
     * @return ログインしたユーザー
     */
    suspend fun verifyGoogleToken(idToken: String): Result<User>

    /**
     * アクセストークンを更新
     */
    suspend fun refreshAccessToken(): Result<Unit>

    /**
     * 現在のユーザー情報を取得
     */
    suspend fun getCurrentUser(): Result<User>

    /**
     * ログアウト
     */
    suspend fun logout(): Result<Unit>

    /**
     * ユーザー名を更新
     * @param name 新しいユーザー名
     * @return 更新後のユーザー
     */
    suspend fun updateUserName(name: String): Result<User>

    /**
     * 永続化されたセッションを復元
     * @return 復元されたユーザー（未保存の場合はnull）
     */
    suspend fun restoreSession(): Result<User?>
}
