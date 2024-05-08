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

package me.proton.core.userrecovery.data.usecase

import app.cash.turbine.test
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.usecase.ObserveUser
import me.proton.core.userrecovery.data.mock.mockAccount
import me.proton.core.userrecovery.data.mock.mockUser
import me.proton.core.userrecovery.data.mock.mockUserSettings
import me.proton.core.userrecovery.domain.IsDeviceRecoveryEnabled
import me.proton.core.usersettings.domain.usecase.ObserveUserSettings
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveUserDeviceRecoveryTest {
    @MockK
    private lateinit var accountManager: AccountManager

    @MockK
    private lateinit var isDeviceRecoveryEnabled: IsDeviceRecoveryEnabled

    @MockK
    private lateinit var observeUser: ObserveUser

    @MockK
    private lateinit var observeUserSettings: ObserveUserSettings

    private lateinit var tested: ObserveUserDeviceRecovery

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        tested = ObserveUserDeviceRecovery(accountManager, isDeviceRecoveryEnabled, observeUser, observeUserSettings)
    }

    @Test
    fun `user recovery setting is disabled`() = runTest {
        // GIVEN
        val testUserId = UserId("user_id_1")
        val user = mockUser(testUserId)
        every { accountManager.getAccounts() } returns MutableStateFlow(listOf(mockAccount(testUserId)))
        every { isDeviceRecoveryEnabled(testUserId) } returns true
        every { observeUser(testUserId) } returns MutableStateFlow(user)
        every { observeUserSettings(testUserId) } returns MutableStateFlow(mockUserSettings(testUserId, false))

        // WHEN
        tested().test {
            // THEN
            assertEquals(Pair(user, false), awaitItem())
        }
    }

    @Test
    fun `device recovery flag is disabled`() = runTest {
        // GIVEN
        val testUserId = UserId("user_id_1")
        val user = mockUser(testUserId)
        every { accountManager.getAccounts() } returns MutableStateFlow(listOf(mockAccount(testUserId)))
        every { isDeviceRecoveryEnabled(testUserId) } returns false
        every { observeUser(testUserId) } returns MutableStateFlow(user)
        every { observeUserSettings(testUserId) } returns MutableStateFlow(mockUserSettings(testUserId, true))

        // WHEN
        tested().test {
            // THEN
            expectNoEvents()
        }
    }

    @Test
    fun `multiple accounts`() = runTest {
        // GIVEN
        val testUserId1 = UserId("user_id_1")
        val testUserId2 = UserId("user_id_2")
        val user1 = mockUser(testUserId1)
        val user2 = mockUser(testUserId2)
        val userSettingsFlow1 = MutableStateFlow(mockUserSettings(testUserId1, false))
        val userSettingsFlow2 = MutableStateFlow(mockUserSettings(testUserId2, false))

        every { accountManager.getAccounts() } returns MutableStateFlow(
            listOf(
                mockAccount(testUserId1),
                mockAccount(testUserId2)
            )
        )
        every { isDeviceRecoveryEnabled(any()) } returns true
        every { observeUser(testUserId1) } returns MutableStateFlow(user1)
        every { observeUser(testUserId2) } returns MutableStateFlow(user2)
        every { observeUserSettings(testUserId1) } returns userSettingsFlow1
        every { observeUserSettings(testUserId2) } returns userSettingsFlow2

        // WHEN
        tested().test {
            // THEN
            assertEquals(Pair(user1, false), awaitItem())
            assertEquals(Pair(user2, false), awaitItem())

            // WHEN
            userSettingsFlow1.value = mockUserSettings(testUserId1, true)

            // THEN
            assertEquals(Pair(user1, true), awaitItem())
        }
    }
}
