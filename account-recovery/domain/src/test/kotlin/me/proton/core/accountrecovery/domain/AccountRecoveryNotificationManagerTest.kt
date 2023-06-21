/*
 * Copyright (c) 2023 Proton AG
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

package me.proton.core.accountrecovery.domain

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.justRun
import io.mockk.verify
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.UserRecovery
import kotlin.test.BeforeTest
import kotlin.test.Test

class AccountRecoveryNotificationManagerTest {
    @MockK
    private lateinit var cancelNotifications: CancelNotifications

    @MockK
    private lateinit var configureAccountRecoveryChannel: ConfigureAccountRecoveryChannel

    @MockK
    private lateinit var getAccountRecoveryChannelId: GetAccountRecoveryChannelId

    @MockK
    private lateinit var isAccountRecoveryEnabled: IsAccountRecoveryEnabled

    @MockK
    private lateinit var showNotification: ShowNotification

    private lateinit var tested: AccountRecoveryNotificationManager

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        tested = AccountRecoveryNotificationManager(
            cancelNotifications,
            configureAccountRecoveryChannel,
            getAccountRecoveryChannelId,
            isAccountRecoveryEnabled,
            showNotification
        )
    }

    @Test
    fun noSetupIfDisabled() {
        // GIVEN
        every { isAccountRecoveryEnabled() } returns false

        // WHEN
        tested.setupNotificationChannel()

        // THEN
        verify(exactly = 0) { getAccountRecoveryChannelId() }
        verify(exactly = 0) { configureAccountRecoveryChannel(any()) }
    }

    @Test
    fun setupIfEnabled() {
        // GIVEN
        every { isAccountRecoveryEnabled() } returns true
        every { getAccountRecoveryChannelId() } returns "channel-id"
        justRun { configureAccountRecoveryChannel(any()) }

        // WHEN
        tested.setupNotificationChannel()

        // THEN
        verify(exactly = 1) { getAccountRecoveryChannelId() }
        verify(exactly = 1) { configureAccountRecoveryChannel("channel-id") }
    }

    @Test
    fun noNotificationIfDisabled() {
        // GIVEN
        every { isAccountRecoveryEnabled() } returns false

        // WHEN
        tested.updateNotification(UserRecovery.State.Grace, UserId("user-id"))

        // THEN
        verify(exactly = 0) { getAccountRecoveryChannelId() }
        verify(exactly = 0) { cancelNotifications(any()) }
        verify(exactly = 0) { showNotification(any(), any()) }
    }

    @Test
    fun cancelingNotification() {
        // GIVEN
        every { isAccountRecoveryEnabled() } returns true
        justRun { cancelNotifications(any()) }

        // WHEN
        tested.updateNotification(UserRecovery.State.None, UserId("user-id"))

        // THEN
        verify(exactly = 1) { cancelNotifications(UserId("user-id")) }
        verify(exactly = 0) { showNotification(any(), any()) }
    }

    @Test
    fun showingNotification() {
        // GIVEN
        every { isAccountRecoveryEnabled() } returns true
        justRun { showNotification(any(), any()) }

        // WHEN
        tested.updateNotification(UserRecovery.State.Insecure, UserId("user-id"))

        // THEN
        verify(exactly = 0) { cancelNotifications(any()) }
        verify(exactly = 1) {
            showNotification(
                UserRecovery.State.Insecure,
                UserId("user-id")
            )
        }
    }
}
