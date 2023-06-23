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

import androidx.lifecycle.DefaultLifecycleObserver
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.onAccountState
import me.proton.core.notification.domain.ProtonNotificationManager
import me.proton.core.notification.domain.usecase.IsNotificationsEnabled
import me.proton.core.notification.domain.usecase.ObservePushNotifications
import me.proton.core.notification.presentation.internal.HasNotificationPermission
import me.proton.core.notification.presentation.ui.NotificationPermissionActivity
import me.proton.core.presentation.app.ActivityProvider
import me.proton.core.presentation.app.AppLifecycleObserver
import me.proton.core.presentation.app.AppLifecycleProvider
import me.proton.core.util.kotlin.CoroutineScopeProvider
import javax.inject.Inject

public class NotificationSetup @Inject internal constructor(
    private val accountManager: AccountManager,
    private val activityProvider: ActivityProvider,
    private val appLifecycleObserver: AppLifecycleObserver,
    private val hasNotificationPermission: HasNotificationPermission,
    private val isNotificationsEnabled: IsNotificationsEnabled,
    private val notificationManager: ProtonNotificationManager,
    private val observePushNotifications: ObservePushNotifications,
    private val scopeProvider: CoroutineScopeProvider
) : DefaultLifecycleObserver {
    public operator fun invoke() {
        if (!isNotificationsEnabled()) return

        accountManager.onAccountState(AccountState.Ready).onEach { account ->
            observePushNotifications(account.userId)
        }.launchIn(scopeProvider.GlobalDefaultSupervisedScope)

        // Setup for notification permissions:
        scopeProvider.GlobalDefaultSupervisedScope.launch {
            waitForConditions()
            setupNotifications()
        }
    }

    /** App in foreground, an account is ready. */
    private suspend fun waitForConditions() {
        appLifecycleObserver.state
            .combine(accountManager.onAccountStateChanged(initialState = true), ::Pair)
            .filter { (state, account) ->
                state == AppLifecycleProvider.State.Foreground && account.state == AccountState.Ready
            }.first()
    }

    private fun setupNotifications() {
        if (hasNotificationPermission()) {
            notificationManager.setupNotificationChannel()
        } else {
            startNotificationPermissionActivity()
        }
    }

    private fun startNotificationPermissionActivity() {
        val activity = activityProvider.lastResumed ?: return
        activity.startActivity(NotificationPermissionActivity(activity))
    }
}
