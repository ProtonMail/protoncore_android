/*
 * Copyright (c) 2024 Proton Technologies AG
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

package me.proton.core.accountmanager.presentation.compose.viewmodel

import app.cash.turbine.test
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.flowOf
import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.AccountDetails
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.account.domain.entity.SessionState
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.getPrimaryAccount
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.Session
import me.proton.core.network.domain.session.SessionId
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.entity.Type
import me.proton.core.user.domain.entity.User
import org.junit.After
import org.junit.Test
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

class AccountSettingsViewModelTest : CoroutinesTest by CoroutinesTest() {
    @MockK
    private lateinit var accountManager: AccountManager
    @MockK
    private lateinit var userManager: UserManager

    private lateinit var tested: AccountSettingsViewModel

    private val userId = UserId("test-user-id")

    private val session = Session.Authenticated(
        userId = userId,
        sessionId = SessionId("session"),
        accessToken = "accessToken",
        refreshToken = "refreshToken",
        scopes = listOf("full", "calendar", "mail")
    )

    private val account = Account(
        userId = userId,
        username = "username",
        email = "test@example.com",
        state = AccountState.Ready,
        sessionId = session.sessionId,
        sessionState = SessionState.Authenticated,
        details = AccountDetails(null, null)
    )

    private val user = User(
        userId = userId,
        email = null,
        name = "test username",
        displayName = null,
        currency = "test-curr",
        credit = 0,
        createdAtUtc = 1000L,
        usedSpace = 0,
        maxSpace = 100,
        maxUpload = 100,
        role = null,
        private = true,
        services = 1,
        subscribed = 0,
        delinquent = null,
        recovery = null,
        keys = emptyList(),
        type = Type.Proton
    )

    @BeforeTest
    fun beforeEveryTest() {
        MockKAnnotations.init(this)
        mockkStatic("me.proton.core.accountmanager.domain.AccountManagerExtensionsKt")
        every { accountManager.getPrimaryAccount() } returns flowOf(account)
        coEvery { userManager.getUser(userId) } returns user
        tested = AccountSettingsViewModel(accountManager, userManager)
    }

    @After
    fun afterEveryTest() {
        unmockkAll()
    }

    @Test
    fun `state logged in test`() = coroutinesTest {
        // WHEN
        tested.state.test {
            // THEN
            assertEquals(AccountSettingsViewState.Hidden, awaitItem())

            val loggedInState = AccountSettingsViewState.LoggedIn(
                userId, "TU", null, null
            )
            assertEquals(loggedInState, awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun `state credentialless test`() = coroutinesTest {
        // GIVEN
        every { accountManager.getPrimaryAccount() } returns flowOf(account)
        coEvery { userManager.getUser(userId) } returns user.copy(type = Type.CredentialLess)
        tested = AccountSettingsViewModel(accountManager, userManager)
        // WHEN
        tested.state.test {
            // THEN
            assertEquals(AccountSettingsViewState.Hidden, awaitItem())

            val loggedInState = AccountSettingsViewState.CredentialLess(userId)
            assertEquals(loggedInState, awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun `state null account test`() = coroutinesTest {
        // GIVEN
        every { accountManager.getPrimaryAccount() } returns flowOf(null)
        coEvery { userManager.getUser(userId) } returns user.copy(type = Type.CredentialLess)
        tested = AccountSettingsViewModel(accountManager, userManager)
        // WHEN
        tested.state.test {
            // THEN
            assertEquals(AccountSettingsViewState.Hidden, awaitItem())
            expectNoEvents()
        }
    }
}
