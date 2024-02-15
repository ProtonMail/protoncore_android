/*
 * Copyright (c) 2021 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
 *
 * ProtonCore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonCore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonCore.  If not, see <https://www.gnu.org/licenses/>.
 */

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
import me.proton.core.auth.domain.entity.SecondFactorMethod
import me.proton.core.auth.domain.entity.SessionInfo
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.Session
import me.proton.core.network.domain.session.SessionId
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

private const val testUsername = "test-username"
private const val testPassword = "test-password"

class CreateLoginSessionTest {
    private lateinit var accountWorkflowHandler: AccountWorkflowHandler
    private lateinit var performLogin: PerformLogin
    private lateinit var tested: CreateLoginSession

    @Before
    fun setUp() {
        accountWorkflowHandler = mockk()
        performLogin = mockk()
        tested = CreateLoginSession(accountWorkflowHandler, performLogin)
    }

    @Test
    fun `basic login session`() = runTest {
        setupMocks()

        val sessionInfo = tested.invoke(testUsername, testPassword, AccountType.Internal)
        coVerify { performLogin.invoke(testUsername, testPassword) }

        val accountSlot = slot<Account>()
        val sessionSlot = slot<Session>()
        coVerify { accountWorkflowHandler.handleSession(capture(accountSlot), capture(sessionSlot)) }

        assertEquals(testUsername, accountSlot.captured.username)
        assertEquals(AccountState.NotReady, accountSlot.captured.state)
        assertEquals(SessionState.Authenticated, accountSlot.captured.sessionState)

        assertEquals(sessionInfo.sessionId, sessionSlot.captured.sessionId)
        assertEquals(sessionInfo.accessToken, sessionSlot.captured.accessToken)
        assertEquals(sessionInfo.refreshToken, sessionSlot.captured.refreshToken)
        assertEquals(sessionInfo.scopes, sessionSlot.captured.scopes)
    }

    @Test
    fun `login session with 2fa`() = runTest {
        setupMocks(SecondFactor.Enabled(setOf(SecondFactorMethod.Authenticator)))

        val sessionInfo = tested.invoke(testUsername, testPassword, AccountType.Internal)
        coVerify { performLogin.invoke(testUsername, testPassword) }

        val accountSlot = slot<Account>()
        val sessionSlot = slot<Session>()
        coVerify { accountWorkflowHandler.handleSession(capture(accountSlot), capture(sessionSlot)) }

        assertEquals(testUsername, accountSlot.captured.username)
        assertEquals(AccountState.NotReady, accountSlot.captured.state)
        assertEquals(SessionState.SecondFactorNeeded, accountSlot.captured.sessionState)

        assertEquals(sessionInfo.sessionId, sessionSlot.captured.sessionId)
        assertEquals(sessionInfo.accessToken, sessionSlot.captured.accessToken)
        assertEquals(sessionInfo.refreshToken, sessionSlot.captured.refreshToken)
        assertEquals(sessionInfo.scopes, sessionSlot.captured.scopes)
    }

    private fun setupMocks(secondFactor: SecondFactor? = null) {
        coEvery { performLogin.invoke(any(), any()) } coAnswers {
            SessionInfo(
                testUsername,
                "access-token",
                "token-type",
                listOf("scope1", "scope2"),
                SessionId("session-id"),
                UserId("user-id"),
                "refresh-token",
                "event-id",
                "server-proof",
                1,
                0,
                secondFactor,
                false
            )
        }

        coJustRun { accountWorkflowHandler.handleSession(any(), any()) }
    }
}
