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

package me.proton.core.notification.presentation

import android.app.Activity
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.notification.domain.ProtonNotificationManager
import me.proton.core.notification.presentation.internal.HasNotificationPermission
import me.proton.core.notification.domain.usecase.IsNotificationsEnabled
import me.proton.core.presentation.app.ActivityProvider
import me.proton.core.presentation.app.AppLifecycleObserver
import me.proton.core.presentation.app.AppLifecycleProvider
import me.proton.core.test.kotlin.CoroutinesTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class NotificationSetupTest : CoroutinesTest by CoroutinesTest() {
    @MockK
    private lateinit var accountManager: AccountManager

    @MockK
    private lateinit var activityProvider: ActivityProvider

    @MockK
    private lateinit var appLifecycleObserver: AppLifecycleObserver

    @MockK
    private lateinit var hasNotificationPermission: HasNotificationPermission

    @MockK
    private lateinit var isNotificationsEnabled: IsNotificationsEnabled

    @MockK
    private lateinit var notificationManager: ProtonNotificationManager

    private lateinit var tested: NotificationSetup

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)

        tested = NotificationSetup(
            accountManager,
            activityProvider,
            appLifecycleObserver,
            hasNotificationPermission,
            isNotificationsEnabled,
            notificationManager
        )
    }

    @Test
    fun notificationsDisabled() = coroutinesTest {
        every { isNotificationsEnabled() } returns false
        tested()
        verify { isNotificationsEnabled() }
    }

    @Test
    fun hasNotificationPermission() = coroutinesTest {
        // GIVEN
        val appStateFlow = MutableStateFlow(AppLifecycleProvider.State.Background)
        val accountStateFlow = MutableStateFlow(mockAccount(AccountState.NotReady))

        every { isNotificationsEnabled() } returns true
        every { appLifecycleObserver.state } returns appStateFlow
        every { accountManager.onAccountStateChanged(any()) } returns accountStateFlow
        every { hasNotificationPermission.invoke() } returns true
        justRun { notificationManager.setupNotificationChannel() }

        // WHEN
        launch {
            yield()
            appStateFlow.value = AppLifecycleProvider.State.Foreground
            yield()
            accountStateFlow.value = mockAccount(AccountState.Ready)
        }
        tested()

        // THEN
        verify { notificationManager.setupNotificationChannel() }
    }

    @Test
    fun noNotificationPermission() = coroutinesTest {
        // GIVEN
        every { isNotificationsEnabled() } returns true
        every { appLifecycleObserver.state } returns MutableStateFlow(AppLifecycleProvider.State.Foreground)
        every { accountManager.onAccountStateChanged(any()) } returns flowOf(
            mockAccount(AccountState.Ready)
        )
        every { hasNotificationPermission.invoke() } returns false
        val activity = mockk<Activity> {
            justRun { startActivity(any()) }
            every { packageName } returns "package_name"
        }
        every { activityProvider.lastResumed } returns activity

        // WHEN
        tested()

        // THEN
        verify { activity.startActivity(any()) }
    }

    @Test
    fun noResumedActivity() = coroutinesTest {
        // GIVEN
        every { isNotificationsEnabled() } returns true
        every { appLifecycleObserver.state } returns MutableStateFlow(AppLifecycleProvider.State.Foreground)
        every { accountManager.onAccountStateChanged(any()) } returns flowOf(
            mockAccount(AccountState.Ready)
        )
        every { hasNotificationPermission.invoke() } returns false
        every { activityProvider.lastResumed } returns null

        // WHEN
        tested()

        // THEN
        verify(exactly = 0) { notificationManager.setupNotificationChannel() }
    }

    @Test
    fun appGoesIntoBackground() = coroutinesTest {
        // GIVEN
        val appStateFlow = MutableStateFlow(AppLifecycleProvider.State.Foreground)
        val accountStateFlow = MutableStateFlow(mockAccount(AccountState.NotReady))

        every { isNotificationsEnabled() } returns true
        every { appLifecycleObserver.state } returns appStateFlow
        every { accountManager.onAccountStateChanged(any()) } returns accountStateFlow
        every { hasNotificationPermission.invoke() } returns false

        // WHEN
        launch {
            yield()
            accountStateFlow.value = mockAccount(AccountState.Ready)
            appStateFlow.value = AppLifecycleProvider.State.Background
        }
        tested()

        // THEN
        verify(exactly = 0) { notificationManager.setupNotificationChannel() }
    }

    private fun mockAccount(s: AccountState): Account = mockk {
        every { state } returns s
    }
}
