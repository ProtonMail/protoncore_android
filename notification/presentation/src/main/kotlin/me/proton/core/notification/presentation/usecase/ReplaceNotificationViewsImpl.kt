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

package me.proton.core.notification.presentation.usecase

import android.app.NotificationManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import me.proton.core.domain.entity.UserId
import me.proton.core.notification.domain.entity.Notification
import me.proton.core.notification.domain.entity.NotificationId
import me.proton.core.notification.domain.usecase.CancelNotificationView
import me.proton.core.notification.domain.usecase.ReplaceNotificationViews
import me.proton.core.notification.domain.usecase.ShowNotificationView
import me.proton.core.notification.domain.usecase.ShowNotificationView.Companion.ExtraNotificationId
import me.proton.core.notification.domain.usecase.ShowNotificationView.Companion.ExtraUserId
import javax.inject.Inject

public class ReplaceNotificationViewsImpl @Inject constructor(
    private val cancelNotificationView: CancelNotificationView,
    @ApplicationContext private val context: Context,
    private val showNotificationView: ShowNotificationView
) : ReplaceNotificationViews {
    private val notificationManager: NotificationManager
        get() = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override fun invoke(notifications: List<Notification>, userId: UserId) {
        val activeNotificationIds = notificationManager.getActiveNotificationIds(userId)
        val currentNotificationIds = notifications.map { it.notificationId }.toSet()
        val notificationIdsToDismiss = activeNotificationIds - currentNotificationIds

        notificationIdsToDismiss.forEach {
            cancelNotificationView(it, userId)
        }

        notifications.forEach {
            showNotificationView(it)
        }
    }

    private fun NotificationManager.getActiveNotificationIds(userId: UserId): Set<NotificationId> =
        activeNotifications
            .filter { statusBarNotification ->
                statusBarNotification.notification.extras.getString(ExtraUserId) == userId.id
            }.mapNotNull { statusBarNotification ->
                statusBarNotification.notification.extras.getString(ExtraNotificationId)
                    ?.let { NotificationId(it) }
            }.toSet()
}
