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
import kotlinx.coroutines.flow.first
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.onAccountState
import me.proton.core.notification.domain.ProtonNotificationManager
import me.proton.core.notification.presentation.internal.HasNotificationPermission
import me.proton.core.notification.presentation.ui.NotificationPermissionActivity
import me.proton.core.notification.domain.usecase.IsNotificationsEnabled
import me.proton.core.presentation.app.ActivityProvider
import me.proton.core.presentation.app.AppLifecycleObserver
import me.proton.core.presentation.app.AppLifecycleProvider
import javax.inject.Inject

public class NotificationSetup @Inject internal constructor(
    private val accountManager: AccountManager,
    private val activityProvider: ActivityProvider,
    private val appLifecycleObserver: AppLifecycleObserver,
    private val hasNotificationPermission: HasNotificationPermission,
    private val isNotificationsEnabled: IsNotificationsEnabled,
    private val notificationManager: ProtonNotificationManager
) : DefaultLifecycleObserver {
    public suspend operator fun invoke() {
        if (!isNotificationsEnabled()) return

        waitForForeground()
        waitForAccountStateReady()
        setupNotifications()
    }

    private suspend fun waitForForeground() =
        appLifecycleObserver.state.first { it == AppLifecycleProvider.State.Foreground }

    private suspend fun waitForAccountStateReady() =
        accountManager.onAccountState(AccountState.Ready).first()

    private fun setupNotifications() {
        if (hasNotificationPermission()) {
            notificationManager.setupNotificationChannel()
        } else {
            startNotificationPermissionActivity()
        }
    }

    private fun startNotificationPermissionActivity() {
        if (appLifecycleObserver.state.value != AppLifecycleProvider.State.Foreground) return
        val activity = activityProvider.lastResumed ?: return
        activity.startActivity(NotificationPermissionActivity(activity))
    }
}
