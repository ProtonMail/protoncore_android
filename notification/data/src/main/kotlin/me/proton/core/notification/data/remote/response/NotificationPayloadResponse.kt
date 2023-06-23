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
import me.proton.core.notification.domain.entity.NotificationPayload
import me.proton.core.util.kotlin.deserializeOrNull

@Serializable
public data class NotificationPayloadResponse(
    @SerialName("Title")
    val title: String? = null,
    @SerialName("Subtitle")
    val subtitle: String? = null,
    @SerialName("Body")
    val body: String? = null
)

internal fun String.toNotificationPayload(): NotificationPayload {
    return when (val response: NotificationPayloadResponse? = this.deserializeOrNull()) {
        null -> NotificationPayload.Unknown(raw = this)
        else -> NotificationPayload.Unencrypted(
            raw = this,
            title = response.title,
            subtitle = response.subtitle,
            body = response.body
        )
    }
}
