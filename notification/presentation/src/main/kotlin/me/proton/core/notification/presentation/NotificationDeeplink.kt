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

package me.proton.core.notification.presentation

import me.proton.core.domain.entity.UserId
import me.proton.core.notification.domain.entity.NotificationId


public object NotificationDeeplink {
    public object Open {
        public const val Deeplink: String =
            "user/{userId}/notification/{notificationId}/open/{type}"

        public fun get(userId: UserId, notificationId: NotificationId, type: String): String =
            "user/${userId.id}/notification/${notificationId.id}/open/$type"
    }

    public object Delete {
        public const val Deeplink: String =
            "user/{userId}/notification/{notificationId}/delete"

        public fun get(userId: UserId, notificationId: NotificationId): String =
            "user/${userId.id}/notification/${notificationId.id}/delete"
    }
}
