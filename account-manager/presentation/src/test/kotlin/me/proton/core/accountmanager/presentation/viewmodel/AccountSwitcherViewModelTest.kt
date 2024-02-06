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

package me.proton.core.accountmanager.presentation.viewmodel

import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
import me.proton.core.test.android.ArchTest
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.assertIs
import me.proton.core.user.domain.UserManager
import kotlin.test.BeforeTest
import kotlin.test.Test

class AccountSwitcherViewModelTest : ArchTest by ArchTest(), CoroutinesTest by CoroutinesTest() {

    private val accountManager = mockk<AccountManager>(relaxed = true)
    private val userManager = mockk<UserManager>(relaxed = true)

    private lateinit var viewModel: AccountSwitcherViewModel

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

    @BeforeTest
    fun beforeEveryTest() {
        viewModel = AccountSwitcherViewModel(accountManager, userManager)
    }

    @Test
    fun `on PrimaryAccount`() = coroutinesTest {
        every { accountManager.getPrimaryAccount() } returns flowOf(account1)
        viewModel.primaryAccount

        verify(exactly = 1) { accountManager.getPrimaryAccount() }
    }

    @Test
    fun `on Action Add`() = coroutinesTest {
        every { accountManager.getPrimaryAccount() } returns flowOf(account1)
        viewModel.onAction().test {
            viewModel.add()
            assertIs<AccountSwitcherViewModel.Action.Add>(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `on Action Remove`() = coroutinesTest {
        every { accountManager.getPrimaryAccount() } returns flowOf(account1)
        every { accountManager.getAccount(userId) } returns flowOf(account1)

        viewModel.onAction().test {
            viewModel.remove(userId)
            assertIs<AccountSwitcherViewModel.Action.Remove>(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `on Action SignOut`() = coroutinesTest {
        every { accountManager.getPrimaryAccount() } returns flowOf(account1)
        every { accountManager.getAccount(userId) } returns flowOf(account1)

        viewModel.onAction().test {
            viewModel.signOut(userId)
            assertIs<AccountSwitcherViewModel.Action.SignOut>(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `on Action Switch Disabled`() = coroutinesTest {
        val account = account1.copy(
            state = AccountState.Disabled
        )
        every { accountManager.getPrimaryAccount() } returns flowOf(account)
        every { accountManager.getAccount(userId) } returns flowOf(account)

        viewModel.onAction().test {
            viewModel.switch(userId)
            assertIs<AccountSwitcherViewModel.Action.SignIn>(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `on Action Switch SetPrimary`() = coroutinesTest {
        val account = account1.copy(
            state = AccountState.Ready
        )
        every { accountManager.getPrimaryAccount() } returns flowOf(account)
        every { accountManager.getAccount(userId) } returns flowOf(account)

        viewModel.onAction().test {
            viewModel.switch(userId)
            assertIs<AccountSwitcherViewModel.Action.SetPrimary>(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }
}