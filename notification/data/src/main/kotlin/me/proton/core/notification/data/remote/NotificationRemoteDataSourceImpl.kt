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

package me.proton.core.notification.data.remote

import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import me.proton.core.notification.data.remote.response.toNotification
import me.proton.core.notification.domain.entity.Notification
import me.proton.core.notification.domain.entity.NotificationId
import me.proton.core.notification.domain.repository.NotificationRemoteDataSource
import javax.inject.Inject

public class NotificationRemoteDataSourceImpl @Inject constructor(
    private val apiProvider: ApiProvider
) : NotificationRemoteDataSource {

    override suspend fun getNotifications(userId: UserId): List<Notification> =
        apiProvider.get<NotificationApi>(userId).invoke {
            getAllNotifications().notifications.map { notificationResponse ->
                notificationResponse.toNotification(userId)
            }
        }.valueOrThrow

    override suspend fun getNotificationsByType(userId: UserId, type: String): List<Notification> =
        apiProvider.get<NotificationApi>(userId).invoke {
            getAllNotifications().notifications.filter { notificationResponse ->
                notificationResponse.type == type
            }.map { notificationResponse ->
                notificationResponse.toNotification(userId)
            }
        }.valueOrThrow
}