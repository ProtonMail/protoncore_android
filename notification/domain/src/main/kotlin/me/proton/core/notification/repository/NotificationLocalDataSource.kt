/*
 * Copyright (c) 2022 Proton Technologies AG
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

package me.proton.core.notification.repository

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import me.proton.core.notification.entity.Notification
import me.proton.core.notification.entity.NotificationId

public interface NotificationLocalDataSource {

    public suspend fun getNotificationById(userId: UserId, notificationId: NotificationId): Notification

    public suspend fun getNotificationsByUser(userId: UserId): List<Notification>

    public fun observeAllNotificationsByUser(userId: UserId): Flow<List<Notification>>

    public suspend fun deleteAllNotificationsByUser(userId: UserId)

    public suspend fun deleteNotificationById(userId: UserId, notificationId: NotificationId)

    public suspend fun upsertNotifications(vararg notifications: Notification)
}