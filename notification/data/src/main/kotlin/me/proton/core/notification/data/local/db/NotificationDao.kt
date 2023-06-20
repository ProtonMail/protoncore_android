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

package me.proton.core.notification.data.local.db

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import me.proton.core.data.room.db.BaseDao
import me.proton.core.domain.entity.UserId
import me.proton.core.notification.domain.entity.NotificationId

@Dao
public abstract class NotificationDao : BaseDao<NotificationEntity>() {

    @Query("SELECT * FROM NotificationEntity WHERE userId = :userId")
    public abstract fun observeAllNotifications(userId: UserId): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM NotificationEntity WHERE userId = :userId")
    public abstract suspend fun getAllNotifications(userId: UserId): List<NotificationEntity>

    @Query("SELECT * FROM NotificationEntity WHERE userId = :userId AND notificationId = :notificationId")
    public abstract suspend fun getNotification(userId: UserId, notificationId: NotificationId): NotificationEntity?

    @Query("DELETE FROM NotificationEntity WHERE userId IN (:userIds)")
    public abstract suspend fun deleteNotifications(vararg userIds: UserId)

    @Query("DELETE FROM NotificationEntity WHERE userId = :userId AND notificationId IN (:notificationIds)")
    public abstract suspend fun deleteNotifications(userId: UserId, vararg notificationIds: NotificationId)
}