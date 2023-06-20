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

package me.proton.core.notification.data.remote.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.domain.entity.UserId
import me.proton.core.notification.domain.entity.Notification
import me.proton.core.notification.domain.entity.NotificationId

@Serializable
public data class NotificationResponse(
    @SerialName("ID")
    val notificationId: String,
    @SerialName("Time")
    val time: Long,
    @SerialName("Type")
    val type: String,
    @SerialName("Title")
    val title: String? = null,
    @SerialName("Subtitle")
    val subtitle: String? = null,
    @SerialName("Body")
    val body: String? = null
)

internal fun NotificationResponse.toNotification(userId: UserId) = Notification(
    notificationId = NotificationId(notificationId),
    userId = userId,
    time = time,
    type = type,
    title = title,
    subtitle = subtitle,
    body = body
)