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
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.auth.domain.feature.IsFido2Enabled
import me.proton.core.domain.entity.UserId
import me.proton.core.telemetry.domain.TelemetryManager
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.user.domain.entity.Type
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.usecase.ObserveUser
import me.proton.core.usersettings.domain.entity.PasswordSetting
import me.proton.core.usersettings.domain.entity.UserSettings
import me.proton.core.usersettings.domain.usecase.ObserveUserSettings
import org.junit.After
import org.junit.Test
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

class AccountSettingsViewModelTest : CoroutinesTest by CoroutinesTest() {

    @MockK
    private lateinit var accountManager: AccountManager

    @MockK
    private lateinit var isFido2Enabled: IsFido2Enabled

    @MockK
    private lateinit var observeUser: ObserveUser

    @MockK
    private lateinit var observeUserSettings: ObserveUserSettings

    @MockK
    private lateinit var telemetryManager: TelemetryManager

    private lateinit var tested: AccountSettingsViewModel

    private val userId = UserId("test-user-id")

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
        flags = emptyMap(),
        type = Type.Proton
    )

    private val userSettings = UserSettings.nil(userId).copy(
        password = PasswordSetting(1, null),
    )

    @BeforeTest
    fun beforeEveryTest() {
        MockKAnnotations.init(this)
        mockkStatic("me.proton.core.accountmanager.domain.AccountManagerExtensionsKt")
        every { accountManager.getPrimaryUserId() } returns flowOf(userId)
        every { isFido2Enabled(any()) } returns false
        coEvery { observeUser(userId) } returns flowOf(user)
        coEvery { observeUserSettings(userId) } returns flowOf(userSettings)
        tested = AccountSettingsViewModel(
            accountManager = accountManager,
            isFido2Enabled = isFido2Enabled,
            observeUser = observeUser,
            observeUserSettings = observeUserSettings,
            telemetryManager = telemetryManager
        )
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
                userId = userId,
                initials = "TU",
                displayName = "test username",
                email = null
            )
            assertEquals(loggedInState, awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun `state credentialless test`() = coroutinesTest {
        // GIVEN
        coEvery { observeUser(userId) } returns flowOf(user.copy(type = Type.CredentialLess))
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
    fun `state managed test`() = coroutinesTest {
        // GIVEN
        coEvery { observeUser(userId) } returns flowOf(user.copy(type = Type.Managed))
        // WHEN
        tested.state.test {
            // THEN
            assertEquals(AccountSettingsViewState.Hidden, awaitItem())

            val loggedInState = AccountSettingsViewState.LoggedIn(
                userId = userId,
                initials = "TU",
                displayName = "test username",
                email = null
            )
            assertEquals(loggedInState, awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun `state external test`() = coroutinesTest {
        // GIVEN
        coEvery { observeUser(userId) } returns flowOf(user.copy(type = Type.External))
        // WHEN
        tested.state.test {
            // THEN
            assertEquals(AccountSettingsViewState.Hidden, awaitItem())

            val loggedInState = AccountSettingsViewState.LoggedIn(
                userId = userId,
                initials = "TU",
                displayName = "test username",
                email = null
            )
            assertEquals(loggedInState, awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun `state null account test`() = coroutinesTest {
        // GIVEN
        every { accountManager.getPrimaryUserId() } returns flowOf(null)
        coEvery { observeUser(userId) } returns flowOf(user.copy(type = Type.CredentialLess))
        // WHEN
        tested.state.test {
            // THEN
            assertEquals(AccountSettingsViewState.Hidden, awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun `product metrics`() = coroutinesTest {
        // GIVEN
        assertEquals("mobile_signup_full", tested.productFlow)
        assertEquals("account.any.signup", tested.productGroup)
    }
}
