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

package me.proton.core.notification.domain.usecase

import me.proton.core.domain.entity.UserId
import me.proton.core.notification.domain.entity.Notification
import me.proton.core.notification.domain.entity.NotificationPayload

public interface ShowNotificationView {

    /**
     * Shows a notification from a [Notification].
     *
     * Any previous notifications for the same [Notification.notificationId] and [Notification.userId] are cancelled.
     */
    public operator fun invoke(notification: Notification)

    /**
     * Shows a notification from a [NotificationPayload].
     *
     * Any previous notifications for the same [notificationId] and [userId] are cancelled.
     */
    public operator fun invoke(
        userId: UserId,
        notificationId: Int,
        notificationTag: String,
        payload: NotificationPayload,
        contentDeeplink: String? = null,
        deleteDeeplink: String? = null,
        dismissible: Boolean = true
    )

    public companion object {
        public const val ExtraNotificationId: String = "me.proton.core.notification.notificationId"
        public const val ExtraUserId: String = "me.proton.core.notification.userId"
    }
}
