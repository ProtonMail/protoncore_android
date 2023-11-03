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

package me.proton.core.notification.domain

import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import me.proton.core.domain.entity.UserId
import me.proton.core.notification.domain.entity.Notification
import me.proton.core.notification.domain.entity.NotificationId
import me.proton.core.notification.domain.repository.NotificationRepository
import me.proton.core.notification.domain.usecase.CancelNotificationView
import me.proton.core.notification.domain.usecase.ConfigureNotificationChannel
import me.proton.core.notification.domain.usecase.GetNotificationChannelId
import me.proton.core.notification.domain.usecase.IsNotificationsEnabled
import me.proton.core.notification.domain.usecase.ReplaceNotificationViews
import me.proton.core.notification.domain.usecase.ShowNotificationView
import me.proton.core.push.domain.entity.PushObjectType
import me.proton.core.push.domain.repository.PushRepository
import me.proton.core.util.kotlin.CoroutineScopeProvider
import javax.inject.Inject

public class ProtonNotificationManager @Inject constructor(
    private val cancelNotificationView: CancelNotificationView,
    private val configureNotificationChannel: ConfigureNotificationChannel,
    private val getNotificationsChannelId: GetNotificationChannelId,
    private val isNotificationsEnabled: IsNotificationsEnabled,
    private val notificationRepository: NotificationRepository,
    private val pushRepository: PushRepository,
    private val replaceNotificationViews: ReplaceNotificationViews,
    private val scopeProvider: CoroutineScopeProvider,
    private val showNotificationView: ShowNotificationView
) {
    public fun setupNotificationChannel() {
        if (!isNotificationsEnabled(userId = null)) return
        val channelId = getNotificationsChannelId()
        configureNotificationChannel(channelId = channelId)
    }

    /** Dismisses a notification with a given [notificationId] and [userId]. */
    public fun dismiss(notificationId: NotificationId, userId: UserId) {
        if (!isNotificationsEnabled(userId)) return
        cancelNotificationView(notificationId, userId)
        onNotificationConsumed(notificationId, userId)
    }

    /** Dismisses a [notification]. */
    public fun dismiss(notification: Notification) {
        if (!isNotificationsEnabled(notification.userId)) return
        cancelNotificationView(notification)
        onNotificationConsumed(notification.notificationId, notification.userId)
    }

    /** Performs necessary actions after a notification was consumed (e.g. opened or dismissed). */
    public fun onNotificationConsumed(notificationId: NotificationId, userId: UserId): Job =
        scopeProvider.GlobalDefaultSupervisedScope.launch {
            pushRepository.getAllPushes(userId, PushObjectType.Notifications)
                .filter { it.objectId == notificationId.id }
                .forEach { pushRepository.deletePush(it.userId, it.pushId) }
            notificationRepository.deleteNotificationById(userId, notificationId)
        }

    /** Shows the given [notifications] and dismisses everything else.
     * Note: It will only dismiss the notifications for the given [userId],
     * that were added via [show] method.
     * If an app shows it own notifications, they won't be affected.
     */
    public fun replace(notifications: List<Notification>, userId: UserId) {
        if (!isNotificationsEnabled(userId)) return
        require(notifications.all { it.userId == userId })
        replaceNotificationViews(notifications, userId)
    }

    /** Shows a given [notification] (or updates existing one). */
    public fun show(notification: Notification) {
        if (!isNotificationsEnabled(notification.userId)) return
        showNotificationView(notification)
    }
}
