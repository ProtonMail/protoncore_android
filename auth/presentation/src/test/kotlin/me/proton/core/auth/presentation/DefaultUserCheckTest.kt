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

package me.proton.core.auth.presentation

import android.content.Context
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.AccountDetails
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.account.domain.entity.SessionState
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.auth.domain.usecase.PostLoginAccountSetup
import me.proton.core.auth.domain.usecase.UserCheckAction
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.Session
import me.proton.core.network.domain.session.SessionId
import me.proton.core.test.android.ArchTest
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.assertEquals
import me.proton.core.test.kotlin.assertIs
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.entity.Delinquent
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.extension.hasSubscription
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DefaultUserCheckTest : ArchTest by ArchTest(), CoroutinesTest by CoroutinesTest() {

    // mocks
    private val userId = UserId("test-user-id")
    @MockK(relaxed = true)
    private lateinit var user: User
    @MockK(relaxed = true)
    private lateinit var context: Context
    @MockK(relaxed = true)
    private lateinit var accountManager: AccountManager
    @MockK(relaxed = true)
    private lateinit var userManager: UserManager

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

    private lateinit var defaultUserCheck: DefaultUserCheck

    @Before
    fun beforeEveryTest() {
        MockKAnnotations.init(this)
        defaultUserCheck = DefaultUserCheck(context, accountManager, userManager)
    }

    @Test
    fun `default user check user invoice delinquent`() = coroutinesTest {
        every { user.delinquent } returns Delinquent.InvoiceDelinquent
        every { context.getString(R.string.auth_user_check_delinquent_error) } returns "test-message"
        val result = defaultUserCheck.invoke(user)

        assertIs<PostLoginAccountSetup.UserCheckResult.Error>(result)
        (result as PostLoginAccountSetup.UserCheckResult.Error).also {
            val message = it.localizedMessage
            val action = it.action

            assertEquals("test-message", message)
            assertIs<UserCheckAction.OpenUrl>(action)
        }
    }

    @Test
    fun `default user check user invoice mail disabled`() = coroutinesTest {
        every { user.delinquent } returns Delinquent.InvoiceMailDisabled
        every { context.getString(R.string.auth_user_check_delinquent_error) } returns "test-message"
        val result = defaultUserCheck.invoke(user)

        assertIs<PostLoginAccountSetup.UserCheckResult.Error>(result)
        (result as PostLoginAccountSetup.UserCheckResult.Error).also {
            val message = it.localizedMessage
            val action = it.action

            assertEquals("test-message", message)
            assertIs<UserCheckAction.OpenUrl>(action)
        }
    }

    @Test
    fun `default user check user no subscription`() = coroutinesTest {
        every { user.delinquent } returns Delinquent.None
        every { user.userId } returns userId
        every { user.subscribed } returns 0
        every { accountManager.getAccounts() } returns flowOf(listOf(account))
        every { context.getString(R.string.auth_user_check_one_free_error) } returns "test-message"
        coEvery { userManager.getUser(userId).subscribed } returns 0

        val result = defaultUserCheck.invoke(user)

        assertIs<PostLoginAccountSetup.UserCheckResult.Error>(result)
        (result as PostLoginAccountSetup.UserCheckResult.Error).also {
            val message = it.localizedMessage
            val action = it.action

            assertEquals("test-message", message)
            assertNull(action)
        }
    }

    @Test
    fun `default user check success`() = coroutinesTest {
        every { user.delinquent } returns Delinquent.None
        every { user.userId } returns userId
        every { user.subscribed } returns 1
        every { accountManager.getAccounts() } returns flowOf(listOf(account))
        every { context.getString(R.string.auth_user_check_one_free_error) } returns "test-message"
        coEvery { userManager.getUser(userId).subscribed } returns 1

        val result = defaultUserCheck.invoke(user)

        assertIs<PostLoginAccountSetup.UserCheckResult.Success>(result)
    }
}
