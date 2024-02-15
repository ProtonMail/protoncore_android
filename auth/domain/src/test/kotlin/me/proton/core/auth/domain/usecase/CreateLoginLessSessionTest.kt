package me.proton.core.auth.domain.usecase

import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.account.domain.entity.SessionState
import me.proton.core.accountmanager.domain.AccountWorkflowHandler
import me.proton.core.auth.domain.entity.SecondFactor
import me.proton.core.auth.domain.entity.SessionInfo
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.Session
import me.proton.core.network.domain.session.SessionId
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class CreateLoginLessSessionTest {
    private lateinit var accountWorkflowHandler: AccountWorkflowHandler
    private lateinit var performLogin: PerformLoginLess
    private lateinit var tested: CreateLoginLessSession

    @Before
    fun setUp() {
        accountWorkflowHandler = mockk()
        performLogin = mockk()
        tested = CreateLoginLessSession(AccountType.External, accountWorkflowHandler, performLogin)
    }

    @Test
    fun `basic login session`() = runTest {
        setupMocks()

        val sessionInfo = tested.invoke()
        coVerify { performLogin.invoke() }

        val accountSlot = slot<Account>()
        val sessionSlot = slot<Session>()
        coVerify {
            accountWorkflowHandler.handleSession(
                capture(accountSlot),
                capture(sessionSlot)
            )
        }

        Assert.assertEquals(AccountState.NotReady, accountSlot.captured.state)
        Assert.assertEquals(SessionState.Authenticated, accountSlot.captured.sessionState)

        Assert.assertEquals(sessionInfo.sessionId, sessionSlot.captured.sessionId)
        Assert.assertEquals(sessionInfo.accessToken, sessionSlot.captured.accessToken)
        Assert.assertEquals(sessionInfo.refreshToken, sessionSlot.captured.refreshToken)
        Assert.assertEquals(sessionInfo.scopes, sessionSlot.captured.scopes)
    }

    private fun setupMocks(secondFactor: SecondFactor? = null) {
        coEvery { performLogin.invoke() } coAnswers {
            SessionInfo(
                username = "null",
                accessToken = "access-token",
                tokenType = "token-type",
                scopes = listOf("scope1", "scope2"),
                sessionId = SessionId("session-id"),
                userId = UserId("user-id"),
                refreshToken = "refresh-token",
                eventId = "event-id",
                serverProof = null,
                localId = 1,
                passwordMode = 0,
                secondFactor = secondFactor,
                temporaryPassword = false
            )
        }

        coJustRun { accountWorkflowHandler.handleSession(any(), any()) }
    }
}