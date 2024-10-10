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
import android.app.NotificationManager
import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.account.domain.entity.isReady
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import me.proton.core.notification.domain.ProtonNotificationManager
import me.proton.core.notification.domain.entity.NotificationId
import me.proton.core.notification.domain.usecase.CancelNotificationView
import me.proton.core.notification.domain.entity.isDismissible
import me.proton.core.notification.domain.repository.NotificationRepository
import me.proton.core.notification.domain.usecase.IsNotificationsEnabled
import me.proton.core.notification.domain.usecase.ObservePushNotifications
import me.proton.core.notification.presentation.deeplink.DeeplinkContext
import me.proton.core.notification.presentation.deeplink.DeeplinkManager
import me.proton.core.notification.presentation.internal.HasNotificationPermission
import me.proton.core.notification.presentation.ui.NotificationPermissionActivity
import me.proton.core.presentation.app.ActivityProvider
import me.proton.core.util.kotlin.CoroutineScopeProvider
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

@Suppress("LongParameterList")
@Singleton
public class NotificationSetup @Inject internal constructor(
    private val accountManager: AccountManager,
    private val activityProvider: ActivityProvider,
    private val hasNotificationPermission: HasNotificationPermission,
    private val isNotificationsEnabled: IsNotificationsEnabled,
    private val protonNotificationManager: ProtonNotificationManager,
    private val notificationRepository: NotificationRepository,
    private val observePushNotifications: ObservePushNotifications,
    private val scopeProvider: CoroutineScopeProvider,
    private val deeplinkManager: DeeplinkManager,
    private val cancelNotificationView: CancelNotificationView
) : DefaultLifecycleObserver {
    private val observeJobMap: MutableMap<UserId, Job> = mutableMapOf()

    public operator fun invoke() {
        if (!isNotificationsEnabled(userId = null)) return

        // Register Notification Deeplink.
        setupDeeplink()

        scopeProvider.GlobalDefaultSupervisedScope.launch {
            // Setup for notification permissions.
            val activity = waitForConditions()
            setupNotifications(activity)
        }

        scopeProvider.GlobalDefaultSupervisedScope.launch {
            // Observe/cancel Push Notifications.
            observeAccountState()
        }
    }

    private suspend fun observeAccountState() {
        accountManager.onAccountStateChanged(initialState = true).onEach { account ->
            when (account.state) {
                AccountState.Ready -> observePushes(account.userId)
                else -> {
                    cancelPushes(account.userId)
                }
            }
        }.collect()
    }

    private fun observePushes(userId: UserId) {
        observeJobMap[userId]?.cancel()
        observeJobMap[userId] = observePushNotifications(userId)
    }

    private fun cancelPushes(userId: UserId) {
        observeJobMap[userId]?.cancel()
        cancelActiveNotifications(userId)
    }

    private fun cancelActiveNotifications(userId: UserId) {
        cancelNotificationView(userId)
    }

    /** App in foreground, an account is ready. */
    @OptIn(FlowPreview::class)
    private suspend fun waitForConditions(): Activity {
        return accountManager.getAccounts()
            .filter { accounts -> accounts.any { it.isReady() } }
            .distinctUntilChanged()
            .flatMapLatest {
                activityProvider.activityFlow
                    .map { it?.get() }
                    .filterNotNull()
                    .filter { !it.isFinishing && !it.isDestroyed }
            }
            .debounce(600.milliseconds)
            .first()
    }

    private fun setupNotifications(activity: Activity) {
        if (hasNotificationPermission()) {
            protonNotificationManager.setupNotificationChannel()
        } else {
            startNotificationPermissionActivity(activity)
        }
    }

    private fun startNotificationPermissionActivity(activity: Activity) {
        activity.startActivity(NotificationPermissionActivity(activity))
    }

    private fun setupDeeplink() {
        deeplinkManager.register(NotificationDeeplink.Delete.Deeplink) { onNotificationDeeplink(it) }
        deeplinkManager.register(NotificationDeeplink.Open.Deeplink) { onNotificationDeeplink(it) }
    }

    private fun onNotificationDeeplink(link: DeeplinkContext): Boolean {
        val userId = UserId(link.args[0])
        val notificationId = NotificationId(link.args[1])
        scopeProvider.GlobalDefaultSupervisedScope.launch {
            val notification = notificationRepository.getNotificationById(userId, notificationId)
            if (notification?.isDismissible == true) {
                protonNotificationManager.onNotificationConsumed(notificationId, userId)
            }
        }
        return true
    }
}
