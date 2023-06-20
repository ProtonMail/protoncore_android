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

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import me.proton.core.domain.entity.UserId
import me.proton.core.notification.domain.entity.Notification
import me.proton.core.notification.domain.entity.NotificationId
import me.proton.core.user.data.entity.UserEntity

@Entity(
    primaryKeys = ["userId", "notificationId"],
    indices = [
        Index("userId"),
        Index("notificationId")
    ],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
public data class NotificationEntity(
    val notificationId: NotificationId,
    val userId: UserId,
    val time: Long,
    val type: String,
    val title: String?,
    val subtitle: String?,
    val body: String?
)

internal fun NotificationEntity.toNotification(): Notification = Notification(
    notificationId = notificationId,
    userId = userId,
    time = time,
    type = type,
    title = title,
    subtitle = subtitle,
    body = body
)

internal fun Notification.toNotificationEntity(): NotificationEntity = NotificationEntity(
    notificationId = notificationId,
    userId = userId,
    time = time,
    type = type,
    title = title,
    subtitle = subtitle,
    body = body
)