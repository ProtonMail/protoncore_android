/*
 * Copyright (c) 2022 Proton Technologies AG
 * This file is part of Proton AG and ProtonCore.
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

package me.proton.core.accountmanager.domain

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.AccountDetails
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.account.domain.entity.SessionState
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.Session
import me.proton.core.network.domain.session.SessionId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AccountManagerExtensionsKtTest {

    private val accountManager = mockk<AccountManager>(relaxed = true)

    private val userId = UserId("user1")
    private val session1 = Session.Authenticated(
        userId = userId,
        sessionId = SessionId("session1"),
        accessToken = "accessToken",
        refreshToken = "refreshToken",
        scopes = listOf("full", "calendar", "mail")
    )

    private val account1 = Account(
        userId = userId,
        username = "username",
        email = "test@example.com",
        state = AccountState.Ready,
        sessionId = session1.sessionId,
        sessionState = SessionState.Authenticated,
        details = AccountDetails(null, null)
    )

    @Test
    fun `on AccountState with matching state`() = runTest {
        every { accountManager.onAccountStateChanged(any()) } returns flowOf(account1)

        val result = mutableListOf<Account>()
        accountManager.onAccountState(AccountState.Ready).collect {
            result.add(it)
        }
        assertEquals(1, result.size)
        assertEquals(account1, result[0])
    }

    @Test
    fun `on AccountState no matching state`() = runTest {
        every { accountManager.onAccountStateChanged(any()) } returns flowOf(account1)

        val result = mutableListOf<Account>()
        accountManager.onAccountState(AccountState.NotReady).collect {
            result.add(it)
        }
        assertEquals(0, result.size)
    }

    @Test
    fun `on SessionState with matching state`() = runTest {
        every { accountManager.onSessionStateChanged(any()) } returns flowOf(account1)

        val result = mutableListOf<Account>()
        accountManager.onSessionState(SessionState.Authenticated).collect {
            result.add(it)
        }
        assertEquals(1, result.size)
        assertEquals(account1, result[0])
    }

    @Test
    fun `on SessionState no matching state`() = runTest {
        every { accountManager.onSessionStateChanged(any()) } returns flowOf(account1)

        val result = mutableListOf<Account>()
        accountManager.onSessionState(SessionState.SecondFactorNeeded).collect {
            result.add(it)
        }
        assertEquals(0, result.size)
    }

    @Test
    fun `on SessionState with multiple accounts`() = runTest {
        val account2 = account1.copy(
            sessionState = SessionState.SecondFactorNeeded
        )
        every { accountManager.onSessionStateChanged(any()) } returns flowOf(account1, account2)

        val result = mutableListOf<Account>()
        accountManager.onSessionState(SessionState.SecondFactorNeeded).collect {
            result.add(it)
        }
        assertEquals(1, result.size)
        assertEquals(account2, result[0])
    }

    @Test
    fun `on getPrimaryAccount with primary account`() = runTest {
        every { accountManager.getPrimaryUserId() } returns flowOf(userId)
        every { accountManager.getAccount(userId) } returns flowOf(account1)

        val result = mutableListOf<Account?>()
        accountManager.getPrimaryAccount().collect {
            result.add(it)
        }
        assertEquals(1, result.size)
        assertEquals(account1, result[0])
    }

    @Test
    fun `on getPrimaryAccount with no primary account`() = runTest {
        every { accountManager.getPrimaryUserId() } returns flowOf(null)
        every { accountManager.getAccount(userId) } returns flowOf(account1)

        val result = mutableListOf<Account?>()
        accountManager.getPrimaryAccount().collect {
            result.add(it)
        }
        assertEquals(1, result.size)
        assertNull(result[0])
    }

    @Test
    fun `on getAccounts with specific AccountState`() = runTest {
        val account2 = account1.copy(
            state = AccountState.NotReady
        )
        every { accountManager.getAccounts() } returns flowOf(listOf(account1, account2))

        val result = mutableListOf<Account>()
        accountManager.getAccounts(AccountState.Ready).collect {
            result.addAll(it)
        }
        assertEquals(1, result.size)
        assertEquals(account1, result[0])
    }

    @Test
    fun `on getAccounts with specific AccountState multiple accounts`() = runTest {
        val account2 = account1.copy(
            userId = UserId("user2")
        )
        every { accountManager.getAccounts() } returns flowOf(listOf(account1, account2))

        val result = mutableListOf<Account>()
        accountManager.getAccounts(AccountState.Ready).collect {
            result.addAll(it)
        }
        assertEquals(2, result.size)
        assertEquals(account1, result[0])
        assertEquals(account2, result[1])
    }
}
