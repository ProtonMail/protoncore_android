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

package me.proton.core.notification.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.proton.core.domain.entity.UserId
import me.proton.core.notification.data.local.db.NotificationDatabase
import me.proton.core.notification.data.local.db.toNotification
import me.proton.core.notification.data.local.db.toNotificationEntity
import me.proton.core.notification.domain.entity.Notification
import me.proton.core.notification.domain.entity.NotificationId
import me.proton.core.notification.domain.repository.NotificationLocalDataSource
import javax.inject.Inject

public class NotificationLocalDataSourceImpl @Inject constructor(
    notificationDatabase: NotificationDatabase
) : NotificationLocalDataSource {

    private val dao = notificationDatabase.notificationDao()

    override suspend fun getNotificationById(userId: UserId, notificationId: NotificationId): Notification? {
        return dao.getNotification(userId, notificationId)?.toNotification()
    }

    override suspend fun getNotificationsByUser(userId: UserId): List<Notification> {
        return dao.getAllNotifications(userId).map { it.toNotification() }
    }

    override fun observeAllNotificationsByUser(userId: UserId): Flow<List<Notification>> {
        return dao.observeAllNotifications(userId).map { notificationEntities ->
            notificationEntities.map { notificationEntity -> notificationEntity.toNotification() }
        }
    }

    override suspend fun deleteAllNotificationsByUser(vararg userIds: UserId) {
        dao.deleteNotifications(*userIds)
    }

    override suspend fun deleteNotificationsById(userId: UserId, vararg notificationIds: NotificationId) {
        dao.deleteNotifications(userId, *notificationIds)
    }

    override suspend fun upsertNotifications(vararg notifications: Notification) {
        dao.insertOrUpdate(*notifications.map { it.toNotificationEntity() }.toTypedArray())
    }

}