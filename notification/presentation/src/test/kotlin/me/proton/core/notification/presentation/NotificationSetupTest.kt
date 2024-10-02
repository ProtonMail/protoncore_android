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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import me.proton.core.notification.domain.ProtonNotificationManager
import me.proton.core.notification.domain.repository.NotificationRepository
import me.proton.core.notification.domain.usecase.IsNotificationsEnabled
import me.proton.core.notification.domain.usecase.ObservePushNotifications
import me.proton.core.notification.presentation.deeplink.DeeplinkManager
import me.proton.core.notification.presentation.internal.HasNotificationPermission
import me.proton.core.presentation.app.ActivityProvider
import me.proton.core.test.kotlin.TestCoroutineScopeProvider
import me.proton.core.test.kotlin.UnconfinedTestCoroutineScopeProvider
import java.lang.ref.WeakReference
import kotlin.test.BeforeTest
import kotlin.test.Test

class NotificationSetupTest {
    @MockK
    private lateinit var accountManager: AccountManager

    @MockK
    private lateinit var activityProvider: ActivityProvider

    @MockK
    private lateinit var hasNotificationPermission: HasNotificationPermission

    @MockK
    private lateinit var isNotificationsEnabled: IsNotificationsEnabled

    @MockK
    private lateinit var notificationManager: ProtonNotificationManager

    @MockK
    private lateinit var notificationRepository: NotificationRepository

    @MockK
    private lateinit var observePushNotifications: ObservePushNotifications

    @MockK(relaxed = true)
    private lateinit var deeplinkManager: DeeplinkManager

    private lateinit var scopeProvider: TestCoroutineScopeProvider

    private lateinit var tested: NotificationSetup

    private val testUserId = UserId("test_user_id")

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)

        scopeProvider = UnconfinedTestCoroutineScopeProvider()
        tested = NotificationSetup(
            accountManager,
            activityProvider,
            hasNotificationPermission,
            isNotificationsEnabled,
            notificationManager,
            notificationRepository,
            observePushNotifications,
            scopeProvider,
            deeplinkManager
        )
    }

    @Test
    fun notificationsDisabled() = runTest {
        every { isNotificationsEnabled(any()) } returns false
        tested()
        runCurrent()
        verify { isNotificationsEnabled(any()) }
    }

    @Test
    fun notificationsEnabled() = runTest {
        // GIVEN
        val account = mockAccount(AccountState.NotReady)
        val accountStateFlow = MutableStateFlow(account)

        every { isNotificationsEnabled(any()) } returns true
        every { accountManager.getAccounts() } returns MutableStateFlow(listOf(account))
        every { accountManager.onAccountStateChanged(any()) } returns accountStateFlow
        every { hasNotificationPermission.invoke() } returns true
        justRun { notificationManager.setupNotificationChannel() }
        every { observePushNotifications(any()) } returns mockk()

        // WHEN
        launch {
            yield()
            accountStateFlow.value = mockAccount(AccountState.Ready)
        }
        tested()

        // THEN
        verify { deeplinkManager.register(NotificationDeeplink.Delete.Deeplink, any()) }
    }

    @Test
    fun hasNotificationPermission() = runTest {
        // GIVEN
        val account = mockAccount(AccountState.NotReady)
        val allAccountsFlow = MutableStateFlow(listOf(account))
        val accountStateFlow = MutableStateFlow(account)
        val activityStateFlow = MutableStateFlow<WeakReference<Activity>?>(null)

        every { isNotificationsEnabled(any()) } returns true
        every { accountManager.getAccounts() } returns allAccountsFlow
        every { accountManager.onAccountStateChanged(any()) } returns accountStateFlow
        every { activityProvider.activityFlow } returns activityStateFlow
        every { hasNotificationPermission.invoke() } returns true
        justRun { notificationManager.setupNotificationChannel() }
        every { observePushNotifications(any()) } returns mockk()

        // WHEN
        launch {
            yield()
            activityStateFlow.value = WeakReference(mockActivity())
            yield()
            allAccountsFlow.value = listOf(mockAccount(AccountState.Ready))
            accountStateFlow.value = mockAccount(AccountState.Ready)
        }
        tested()
        runCurrent()
        scopeProvider.GlobalDefaultSupervisedScope.advanceUntilIdle()

        // THEN
        verify { notificationManager.setupNotificationChannel() }
        verify { observePushNotifications(testUserId) }
    }

    @Test
    fun noNotificationPermission() = runTest {
        // GIVEN
        val account = mockAccount(AccountState.Ready)
        val accountStateFlow = MutableStateFlow(account)
        val activity = mockActivity()
        val activityStateflow = MutableStateFlow(WeakReference(activity))

        every { isNotificationsEnabled(any()) } returns true
        every { accountManager.getAccounts() } returns MutableStateFlow(listOf(account))
        every { accountManager.onAccountStateChanged(any()) } returns accountStateFlow
        every { activityProvider.activityFlow } returns activityStateflow
        every { hasNotificationPermission.invoke() } returns false
        every { observePushNotifications(any()) } returns mockk()

        // WHEN
        tested()
        runCurrent()
        scopeProvider.GlobalDefaultSupervisedScope.advanceUntilIdle()

        // THEN
        verify { hasNotificationPermission.invoke() }
        verify { activity.startActivity(any()) }
    }

    @Test
    fun noResumedActivity() = runTest {
        // GIVEN
        val account = mockAccount(AccountState.Ready)
        val accountStateFlow = MutableStateFlow(account)
        val activityStateFlow = MutableStateFlow(WeakReference(mockActivity()))

        every { isNotificationsEnabled(any()) } returns true
        every { accountManager.getAccounts() } returns MutableStateFlow(listOf(account))
        every { accountManager.onAccountStateChanged(any()) } returns accountStateFlow
        every { activityProvider.activityFlow } returns activityStateFlow
        every { hasNotificationPermission.invoke() } returns false
        every { observePushNotifications(any()) } returns mockk()

        // WHEN
        tested()
        runCurrent()

        // THEN
        verify(exactly = 0) { notificationManager.setupNotificationChannel() }
    }

    @Test
    fun appGoesIntoBackground() = runTest {
        // GIVEN
        val account = mockAccount(AccountState.NotReady)
        val accountStateFlow = MutableStateFlow(account)

        every { isNotificationsEnabled(any()) } returns true
        every { accountManager.getAccounts() } returns MutableStateFlow(listOf(account))
        every { accountManager.onAccountStateChanged(any()) } returns accountStateFlow
        every { hasNotificationPermission.invoke() } returns false
        every { observePushNotifications(any()) } returns mockk()

        // WHEN
        launch {
            yield()
            accountStateFlow.value = mockAccount(AccountState.Ready)
        }
        tested()
        runCurrent()

        // THEN
        verify(exactly = 0) { notificationManager.setupNotificationChannel() }
    }

    @Test
    fun observeReadyAccount() = runTest {
        // GIVEN
        val account = mockAccount(AccountState.NotReady)
        val accountStateFlow = MutableStateFlow(account)
        val activityStateflow = MutableStateFlow(WeakReference(mockActivity()))

        every { isNotificationsEnabled(any()) } returns true
        every { accountManager.getAccounts() } returns MutableStateFlow(listOf(account))
        every { accountManager.onAccountStateChanged(any()) } returns accountStateFlow
        every { activityProvider.activityFlow } returns activityStateflow
        every { hasNotificationPermission.invoke() } returns true
        justRun { notificationManager.setupNotificationChannel() }
        every { observePushNotifications(any()) } returns mockk()

        // WHEN
        launch {
            yield()
            accountStateFlow.value = mockAccount(AccountState.Ready)
        }
        tested()
        runCurrent()

        // THEN
        verify { observePushNotifications(testUserId) }
    }

    @Test
    fun cancelObserveNonReadyAccount() = runTest {
        // GIVEN
        val account = mockAccount(AccountState.NotReady)
        val accountStateFlow = MutableStateFlow(mockAccount(AccountState.NotReady))
        val activityStateFlow = MutableStateFlow(WeakReference(mockActivity()))
        val observeJob = mockk<Job>(relaxed = true)

        every { isNotificationsEnabled(any()) } returns true
        every { accountManager.getAccounts() } returns MutableStateFlow(listOf(account))
        every { accountManager.onAccountStateChanged(any()) } returns accountStateFlow
        every { activityProvider.activityFlow } returns activityStateFlow
        every { hasNotificationPermission.invoke() } returns true
        justRun { notificationManager.setupNotificationChannel() }
        every { observePushNotifications(any()) } returns observeJob

        // WHEN
        launch {
            yield()
            accountStateFlow.value = mockAccount(AccountState.Ready)
            yield()
            accountStateFlow.value = mockAccount(AccountState.NotReady)
        }
        tested()
        runCurrent()

        // THEN
        verify { observePushNotifications(testUserId) }
        verify { observeJob.cancel(any()) }
    }

    private fun mockAccount(s: AccountState, userId: UserId = testUserId): Account =
        mockk {
            every { state } returns s
            every { this@mockk.userId } returns userId
        }

    private fun mockActivity(
        isDestroyed: Boolean = false,
        isFinishing: Boolean = false
    ) = mockk<Activity>(relaxed = true) {
        every { this@mockk.isDestroyed } returns isDestroyed
        every { this@mockk.isFinishing } returns isFinishing
    }
}
