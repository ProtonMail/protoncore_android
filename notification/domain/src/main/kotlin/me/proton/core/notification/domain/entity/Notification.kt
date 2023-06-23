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

package me.proton.core.notification.domain.entity

import kotlinx.serialization.Serializable
import me.proton.core.domain.entity.UserId

@Serializable
public data class NotificationId(val id: String)

public data class Notification(
    val notificationId: NotificationId,
    val userId: UserId,
    val time: Long,
    val type: String,
    val payload: NotificationPayload
)

public sealed class NotificationPayload(
    public open val raw: String
) {
    public data class Unknown(
        override val raw: String
    ) : NotificationPayload(raw)

    public data class Unencrypted(
        override val raw: String,
        val title: String? = null,
        val subtitle: String? = null,
        val body: String? = null
    ) : NotificationPayload(raw)
}
