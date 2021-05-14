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

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.AccountDetails
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.account.domain.entity.SessionState
import me.proton.core.domain.entity.Product
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.Session
import me.proton.core.network.domain.session.SessionId
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class SessionManagerImplTest {

    private lateinit var accountManager: AccountManagerImpl
    private lateinit var sessionManager: SessionManagerImpl

    private val session1 = Session(
        sessionId = SessionId("session1"),
        accessToken = "accessToken",
        refreshToken = "refreshToken",
        scopes = listOf("full", "calendar", "mail")
    )

    private val account1 = Account(
        userId = UserId("user1"),
        username = "username",
        email = "test@example.com",
        state = AccountState.Ready,
        sessionId = session1.sessionId,
        sessionState = SessionState.Authenticated,
        details = AccountDetails(null)
    )

    private val mocks = RepositoryMocks(session1, account1)

    @Before
    fun beforeEveryTest() {
        mocks.init()

        accountManager = AccountManagerImpl(
            Product.Calendar,
            mocks.accountRepository,
            mocks.authRepository,
            mocks.userManager
        )
        sessionManager = SessionManagerImpl(
            mocks.sessionProvider,
            mocks.sessionListener,
            mocks.authRepository
        )
    }

    @Test
    fun `on onSessionTokenRefreshed`() = runBlockingTest {
        mocks.setupAccountRepository()

        val newAccessToken = "newAccessToken"
        val newRefreshToken = "newRefreshToken"

        sessionManager.onSessionTokenRefreshed(
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
        mocks.setupAccountRepository()

        sessionManager.onSessionForceLogout(session1)

        val stateLists = accountManager.onAccountStateChanged().toList()
        assertEquals(1, stateLists.size)
        assertEquals(AccountState.Disabled, stateLists[0].state)

        val sessionStateLists = accountManager.onSessionStateChanged().toList()
        assertEquals(1, sessionStateLists.size)
        assertEquals(SessionState.ForceLogout, sessionStateLists[0].sessionState)
    }
}
