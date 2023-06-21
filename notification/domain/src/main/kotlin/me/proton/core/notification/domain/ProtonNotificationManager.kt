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

import me.proton.core.domain.entity.UserId
import me.proton.core.notification.domain.entity.Notification
import me.proton.core.notification.domain.entity.NotificationId
import me.proton.core.notification.domain.usecase.CancelNotificationView
import me.proton.core.notification.domain.usecase.ConfigureNotificationChannel
import me.proton.core.notification.domain.usecase.GetNotificationChannelId
import me.proton.core.notification.domain.usecase.IsNotificationsEnabled
import me.proton.core.notification.domain.usecase.ShowNotificationView
import javax.inject.Inject

public class ProtonNotificationManager @Inject constructor(
    private val cancelNotificationView: CancelNotificationView,
    private val configureNotificationChannel: ConfigureNotificationChannel,
    private val getNotificationsChannelId: GetNotificationChannelId,
    private val isNotificationsEnabled: IsNotificationsEnabled,
    private val showNotificationView: ShowNotificationView
) {
    public fun setupNotificationChannel() {
        if (!isNotificationsEnabled()) return
        val channelId = getNotificationsChannelId()
        configureNotificationChannel(channelId = channelId)
    }

    /** Dismisses a notification with a given [notificationId] and [userId]. */
    public fun dismiss(notificationId: NotificationId, userId: UserId) {
        if (!isNotificationsEnabled()) return
        cancelNotificationView(notificationId, userId)
    }

    /** Dismisses a [notification]. */
    public fun dismiss(notification: Notification) {
        if (!isNotificationsEnabled()) return
        cancelNotificationView(notification)
    }

    /** Shows a given [notification] (or updates existing one). */
    public fun show(notification: Notification) {
        if (!isNotificationsEnabled()) return
        showNotificationView(notification)
    }
}
