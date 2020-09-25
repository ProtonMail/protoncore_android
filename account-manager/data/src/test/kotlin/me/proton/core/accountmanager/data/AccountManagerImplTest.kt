/*
 * Copyright (c) 2020 Proton Technologies AG
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
package me.proton.core.accountmanager.data

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.account.domain.entity.SessionState
import me.proton.core.account.domain.repository.AccountRepository
import me.proton.core.domain.entity.Product
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.humanverification.HumanVerificationDetails
import me.proton.core.network.domain.humanverification.HumanVerificationHeaders
import me.proton.core.network.domain.humanverification.VerificationMethod
import me.proton.core.network.domain.session.Session
import me.proton.core.network.domain.session.SessionId
import me.proton.core.network.domain.session.SessionListener
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class AccountManagerImplTest {

    private lateinit var accountManager: AccountManagerImpl

    @RelaxedMockK
    private lateinit var accountRepository: AccountRepository

    private val session1 = Session(
        sessionId = SessionId("session1"),
        accessToken = "accessToken",
        refreshToken = "refreshToken",
        scopes = listOf("full", "calendar", "mail"),
        headers = HumanVerificationHeaders("tokenType", "tokenCode")
    )

    private val account1 = Account(
        userId = UserId("user1"),
        username = "username",
        email = "test@example.com",
        state = AccountState.Ready,
        sessionId = session1.sessionId,
        sessionState = SessionState.Authenticated
    )

    private val flowOfAccountLists = mutableListOf<List<Account>>()
    private val flowOfSessionLists = mutableListOf<List<Session>>()

    @Suppress("LongMethod")
    private fun setupUpdateListsFlow() {
        val userIdSlot = slot<UserId>()
        val sessionIdSlot = slot<SessionId>()
        val accountStateSlot = slot<AccountState>()
        val sessionStateSlot = slot<SessionState>()
        val updatedScopesSlot = slot<List<String>>()
        val tokenTypeSlot = slot<String>()
        val tokenCodeSlot = slot<String>()
        val accessTokenSlot = slot<String>()
        val refreshTokenSlot = slot<String>()

        // Initial state.
        flowOfAccountLists.clear()
        flowOfSessionLists.clear()
        flowOfAccountLists.add(listOf(account1))
        flowOfSessionLists.add(listOf(session1))

        // For each updateAccountState -> emit a new updated List<Account> from getAccounts().
        coEvery { accountRepository.updateAccountState(capture(userIdSlot), capture(accountStateSlot)) } answers {
            flowOfAccountLists.add(
                listOf(
                    flowOfAccountLists.last().first { it.userId == userIdSlot.captured }.copy(
                        userId = userIdSlot.captured,
                        state = accountStateSlot.captured
                    )
                )
            )
        }

        // For each updateSessionState -> emit a new updated List<Account> from getAccounts().
        coEvery { accountRepository.updateSessionState(capture(sessionIdSlot), capture(sessionStateSlot)) } answers {
            flowOfAccountLists.add(
                listOf(
                    flowOfAccountLists.last().first { it.sessionId == sessionIdSlot.captured }.copy(
                        sessionId = sessionIdSlot.captured,
                        sessionState = sessionStateSlot.captured
                    )
                )
            )
        }

        // For each updateSessionScopes -> emit a new updated List<Session> from getSessions().
        coEvery { accountRepository.updateSessionScopes(capture(sessionIdSlot), capture(updatedScopesSlot)) } answers {
            flowOfSessionLists.add(
                listOf(
                    flowOfSessionLists.last().first { it.sessionId == sessionIdSlot.captured }.copy(
                        sessionId = sessionIdSlot.captured,
                        scopes = updatedScopesSlot.captured
                    )
                )
            )
        }

        // For each updateSessionHeaders -> emit a new updated List<Session> from getSessions().
        coEvery {
            accountRepository.updateSessionHeaders(
                capture(sessionIdSlot),
                capture(tokenTypeSlot),
                capture(tokenCodeSlot)
            )
        } answers {
            flowOfSessionLists.add(
                listOf(
                    flowOfSessionLists.last().first { it.sessionId == sessionIdSlot.captured }.copy(
                        sessionId = sessionIdSlot.captured,
                        headers = HumanVerificationHeaders(
                            tokenTypeSlot.captured,
                            tokenCodeSlot.captured
                        )
                    )
                )
            )
        }

        // For each updateSessionToken -> emit a new updated List<Session> from getSessions().
        coEvery {
            accountRepository.updateSessionToken(
                capture(sessionIdSlot),
                capture(accessTokenSlot),
                capture(refreshTokenSlot)
            )
        } answers {
            flowOfSessionLists.add(
                listOf(
                    session1.copy(
                        sessionId = sessionIdSlot.captured,
                        accessToken = accessTokenSlot.captured,
                        refreshToken = refreshTokenSlot.captured
                    )
                )
            )
        }

        // Emit last state with same id if exist.
        coEvery { accountRepository.getAccountOrNull(capture(userIdSlot)) } answers {
            flowOfAccountLists.last().firstOrNull { it.userId == userIdSlot.captured }
        }
        coEvery { accountRepository.getAccountOrNull(capture(sessionIdSlot)) } answers {
            flowOfAccountLists.last().firstOrNull { it.sessionId == sessionIdSlot.captured }
        }

        // Emit all state with same id if exist.
        coEvery { accountRepository.getAccount(capture(sessionIdSlot)) } answers {
            val filteredLists = flowOfAccountLists.map { list ->
                list.firstOrNull { it.sessionId == sessionIdSlot.captured }
            }
            flowOf(*filteredLists.toTypedArray())
        }

        // Finally, emit all flow of Lists.
        every { accountRepository.getAccounts() } answers {
            flowOf(*flowOfAccountLists.toTypedArray())
        }
        every { accountRepository.getSessions() } answers {
            flowOf(*flowOfSessionLists.toTypedArray())
        }
    }

    @Before
    fun beforeEveryTest() {
        MockKAnnotations.init(this)

        accountManager = AccountManagerImpl(Product.Calendar, accountRepository)
    }

    @Test
    fun `add user with session`() = runBlockingTest {
        accountManager.addAccount(account1, session1)

        coVerify(exactly = 1) { accountRepository.createOrUpdateAccountSession(any(), any()) }
    }

    @Test
    fun `on account state changed`() = runBlockingTest {
        every { accountRepository.getAccounts() } returns flowOf(
            listOf(account1),
            listOf(account1),
            listOf(account1.copy(state = AccountState.Disabled))
        )

        val accountLists = accountManager.getAccounts().toList()
        assertEquals(3, accountLists.size)

        val stateLists = accountManager.onAccountStateChanged().toList()
        assertEquals(2, stateLists.size)
        assertEquals(account1.state, stateLists[0].state)
        assertEquals(AccountState.Disabled, stateLists[1].state)
    }

    @Test
    fun `on session state changed`() = runBlockingTest {
        every { accountRepository.getAccounts() } returns flowOf(
            listOf(account1),
            listOf(account1),
            listOf(account1.copy(sessionState = SessionState.ForceLogout))
        )

        val accountLists = accountManager.getAccounts().toList()
        assertEquals(3, accountLists.size)

        val stateLists = accountManager.onSessionStateChanged().toList()
        assertEquals(2, stateLists.size)
        assertEquals(account1.sessionState, stateLists[0].sessionState)
        assertEquals(SessionState.ForceLogout, stateLists[1].sessionState)
    }

    @Test
    fun `on handleTwoPassModeSuccess`() = runBlockingTest {
        setupUpdateListsFlow()

        accountManager.handleTwoPassModeSuccess(account1.userId)

        val stateLists = accountManager.onAccountStateChanged().toList()
        assertEquals(3, stateLists.size)
        assertEquals(account1.state, stateLists[0].state)
        assertEquals(AccountState.TwoPassModeSuccess, stateLists[1].state)
        assertEquals(AccountState.Ready, stateLists[2].state)
    }

    @Test
    fun `on handleTwoPassModeFailed`() = runBlockingTest {
        setupUpdateListsFlow()

        accountManager.handleTwoPassModeFailed(account1.userId)

        val stateLists = accountManager.onAccountStateChanged().toList()
        assertEquals(3, stateLists.size)
        assertEquals(account1.state, stateLists[0].state)
        assertEquals(AccountState.TwoPassModeFailed, stateLists[1].state)
        assertEquals(AccountState.Disabled, stateLists[2].state)
    }

    @Test
    fun `on handleSecondFactorSuccess`() = runBlockingTest {
        setupUpdateListsFlow()

        val newScopes = listOf("scope1", "scope2")

        accountManager.handleSecondFactorSuccess(session1.sessionId, newScopes)

        val stateLists = accountManager.onAccountStateChanged().toList()
        assertEquals(1, stateLists.size)
        assertEquals(account1.state, stateLists[0].state)

        val sessionLists = accountManager.getSessions().toList()
        assertEquals(2, sessionLists.size)
        assertEquals(session1.scopes, sessionLists[0][0].scopes)
        assertEquals(newScopes, sessionLists[1][0].scopes)

        val sessionStateLists = accountManager.onSessionStateChanged().toList()
        assertEquals(3, sessionStateLists.size)
        assertEquals(account1.sessionState, sessionStateLists[0].sessionState)
        assertEquals(SessionState.SecondFactorSuccess, sessionStateLists[1].sessionState)
        assertEquals(SessionState.Authenticated, sessionStateLists[2].sessionState)
    }

    @Test
    fun `on handleSecondFactorFailed`() = runBlockingTest {
        setupUpdateListsFlow()

        accountManager.handleSecondFactorFailed(session1.sessionId)

        val stateLists = accountManager.onAccountStateChanged().toList()
        assertEquals(2, stateLists.size)
        assertEquals(account1.state, stateLists[0].state)
        assertEquals(AccountState.Disabled, stateLists[1].state)

        val sessionStateLists = accountManager.onSessionStateChanged().toList()
        assertEquals(2, sessionStateLists.size)
        assertEquals(account1.sessionState, sessionStateLists[0].sessionState)
        assertEquals(SessionState.SecondFactorFailed, sessionStateLists[1].sessionState)
    }

    @Test
    fun `on handleHumanVerificationSuccess`() = runBlockingTest {
        setupUpdateListsFlow()

        val tokenType = "newTokenType"
        val tokenCode = "newTokenCode"
        val headers = HumanVerificationHeaders(tokenType, tokenCode)

        accountManager.handleHumanVerificationSuccess(session1.sessionId, tokenType, tokenCode)

        val stateLists = accountManager.onAccountStateChanged().toList()
        assertEquals(1, stateLists.size)
        assertEquals(account1.state, stateLists[0].state)

        val sessionLists = accountManager.getSessions().toList()
        assertEquals(2, sessionLists.size)
        assertEquals(session1.headers, sessionLists[0][0].headers)
        assertEquals(headers, sessionLists[1][0].headers)

        val sessionStateLists = accountManager.onSessionStateChanged().toList()
        assertEquals(3, sessionStateLists.size)
        assertEquals(account1.sessionState, sessionStateLists[0].sessionState)
        assertEquals(SessionState.HumanVerificationSuccess, sessionStateLists[1].sessionState)
        assertEquals(SessionState.Authenticated, sessionStateLists[2].sessionState)
    }

    @Test
    fun `on handleHumanVerificationFailed`() = runBlockingTest {
        setupUpdateListsFlow()

        accountManager.handleHumanVerificationFailed(session1.sessionId)

        val stateLists = accountManager.onAccountStateChanged().toList()
        assertEquals(2, stateLists.size)
        assertEquals(account1.state, stateLists[0].state)
        assertEquals(AccountState.Disabled, stateLists[1].state)

        val sessionStateLists = accountManager.onSessionStateChanged().toList()
        assertEquals(2, sessionStateLists.size)
        assertEquals(account1.sessionState, sessionStateLists[0].sessionState)
        assertEquals(SessionState.HumanVerificationFailed, sessionStateLists[1].sessionState)
    }

    @Test
    fun `on onSessionTokenRefreshed`() = runBlockingTest {
        setupUpdateListsFlow()

        val newAccessToken = "newAccessToken"
        val newRefreshToken = "newRefreshToken"

        accountManager.onSessionTokenRefreshed(
            session1.refreshWith(
                accessToken = newAccessToken,
                refreshToken = newRefreshToken
            )
        )

        val sessionLists = accountManager.getSessions().toList()
        assertEquals(2, sessionLists.size)
        assertEquals(session1.accessToken, sessionLists[0][0].accessToken)
        assertEquals(session1.refreshToken, sessionLists[0][0].refreshToken)
        assertEquals(newAccessToken, sessionLists[1][0].accessToken)
        assertEquals(newRefreshToken, sessionLists[1][0].refreshToken)
    }

    @Test
    fun `on onSessionForceLogout`() = runBlockingTest {
        setupUpdateListsFlow()

        accountManager.onSessionForceLogout(session1)

        val stateLists = accountManager.onAccountStateChanged().toList()
        assertEquals(2, stateLists.size)
        assertEquals(account1.state, stateLists[0].state)
        assertEquals(AccountState.Disabled, stateLists[1].state)

        val sessionStateLists = accountManager.onSessionStateChanged().toList()
        assertEquals(2, sessionStateLists.size)
        assertEquals(account1.sessionState, sessionStateLists[0].sessionState)
        assertEquals(SessionState.ForceLogout, sessionStateLists[1].sessionState)
    }

    @Test
    fun `on onHumanVerificationNeeded success`() = runBlockingTest {
        setupUpdateListsFlow()

        val humanVerificationDetails = HumanVerificationDetails(
            verificationMethods = listOf(VerificationMethod.EMAIL),
            captchaVerificationToken = null
        )

        coEvery { accountRepository.getAccount(any<SessionId>()) } returns flowOf(
            account1,
            account1.copy(sessionState = SessionState.HumanVerificationNeeded),
            account1.copy(sessionState = SessionState.HumanVerificationSuccess)
        )

        val result = accountManager.onHumanVerificationNeeded(session1, humanVerificationDetails)

        val sessionStateLists = accountManager.onSessionStateChanged().toList()
        assertEquals(2, sessionStateLists.size)
        assertEquals(account1.sessionState, sessionStateLists[0].sessionState)
        assertEquals(SessionState.HumanVerificationNeeded, sessionStateLists[1].sessionState)

        assertEquals(SessionListener.HumanVerificationResult.Success, result)
    }

    @Test
    fun `on onHumanVerificationNeeded failed`() = runBlockingTest {
        setupUpdateListsFlow()

        val humanVerificationDetails = HumanVerificationDetails(
            verificationMethods = listOf(VerificationMethod.EMAIL),
            captchaVerificationToken = null
        )

        coEvery { accountRepository.getAccount(any<SessionId>()) } returns flowOf(
            account1,
            account1.copy(sessionState = SessionState.HumanVerificationNeeded),
            account1.copy(sessionState = SessionState.HumanVerificationFailed)
        )

        val result = accountManager.onHumanVerificationNeeded(session1, humanVerificationDetails)

        val sessionStateLists = accountManager.onSessionStateChanged().toList()
        assertEquals(2, sessionStateLists.size)
        assertEquals(account1.sessionState, sessionStateLists[0].sessionState)
        assertEquals(SessionState.HumanVerificationNeeded, sessionStateLists[1].sessionState)

        assertEquals(SessionListener.HumanVerificationResult.Failure, result)
    }
}
