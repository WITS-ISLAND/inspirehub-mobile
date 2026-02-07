package io.github.witsisland.inspirehub.presentation.viewmodel

import app.cash.turbine.test
import io.github.witsisland.inspirehub.domain.model.User
import io.github.witsisland.inspirehub.domain.repository.FakeAuthRepository
import io.github.witsisland.inspirehub.domain.store.UserStore
import io.github.witsisland.inspirehub.test.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthViewModelTest : MainDispatcherRule() {

    private lateinit var viewModel: AuthViewModel
    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var userStore: UserStore

    @BeforeTest
    fun setup() {
        fakeAuthRepository = FakeAuthRepository()
        userStore = UserStore()
        viewModel = AuthViewModel(fakeAuthRepository, userStore)
    }

    @AfterTest
    fun tearDown() {
        userStore.logout()
        fakeAuthRepository.reset()
    }

    @Test
    fun `verifyGoogleToken - ID Tokenの検証が成功すること`() = runTest {
        val idToken = "test-id-token"
        val mockUser = User(
            id = "user123",
            handle = "testuser",
            email = "test@example.com",
            roleTag = "Backend"
        )
        fakeAuthRepository.verifyGoogleTokenResult = Result.success(mockUser)

        viewModel.verifyGoogleToken(idToken)

        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.error.value)
        assertEquals(1, fakeAuthRepository.verifyGoogleTokenCallCount)
        assertEquals(idToken, fakeAuthRepository.lastIdToken)
    }

    @Test
    fun `verifyGoogleToken - 失敗時にエラーが設定されること`() = runTest {
        val idToken = "invalid-token"
        val errorMessage = "Token verification failed"
        fakeAuthRepository.verifyGoogleTokenResult = Result.failure(
            Exception(errorMessage)
        )

        viewModel.verifyGoogleToken(idToken)

        assertEquals(errorMessage, viewModel.error.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `fetchCurrentUser - ユーザー情報の取得が成功すること`() = runTest {
        val mockUser = User(
            id = "user123",
            handle = "testuser",
            email = "test@example.com",
            roleTag = "Frontend"
        )
        fakeAuthRepository.getCurrentUserResult = Result.success(mockUser)

        viewModel.fetchCurrentUser()

        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.error.value)
        assertEquals(1, fakeAuthRepository.getCurrentUserCallCount)
    }

    @Test
    fun `fetchCurrentUser - 失敗時にエラーが設定されること`() = runTest {
        val errorMessage = "User not found"
        fakeAuthRepository.getCurrentUserResult = Result.failure(
            Exception(errorMessage)
        )

        viewModel.fetchCurrentUser()

        assertEquals(errorMessage, viewModel.error.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `logout - ログアウトが成功すること`() = runTest {
        fakeAuthRepository.logoutResult = Result.success(Unit)

        viewModel.logout()

        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.error.value)
        assertEquals(1, fakeAuthRepository.logoutCallCount)
    }

    @Test
    fun `logout - 失敗時にエラーが設定されること`() = runTest {
        val errorMessage = "Logout failed"
        fakeAuthRepository.logoutResult = Result.failure(
            Exception(errorMessage)
        )

        viewModel.logout()

        assertEquals(errorMessage, viewModel.error.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `clearError - エラー状態がクリアされること`() = runTest {
        fakeAuthRepository.verifyGoogleTokenResult = Result.failure(
            Exception("Test error")
        )
        viewModel.verifyGoogleToken("dummy-token")
        assertNotNull(viewModel.error.value)

        viewModel.clearError()

        assertNull(viewModel.error.value)
    }

    @Test
    fun `currentUser - UserStoreの状態を反映すること`() = runTest {
        val mockUser = User(
            id = "user123",
            handle = "testuser",
            email = "test@example.com"
        )

        userStore.login(mockUser, "access-token", "refresh-token")

        viewModel.currentUser.test {
            assertEquals(mockUser, awaitItem())
        }
    }

    @Test
    fun `isAuthenticated - UserStoreの認証状態を反映すること`() = runTest {
        viewModel.isAuthenticated.test {
            assertFalse(awaitItem())

            val mockUser = User(
                id = "user123",
                handle = "testuser",
                email = "test@example.com"
            )
            userStore.login(mockUser, "access-token", "refresh-token")

            assertTrue(awaitItem())
        }
    }
}
