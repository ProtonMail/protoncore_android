/*
 * Copyright (c) 2024 Proton AG
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

package me.proton.core.userrecovery.presentation.compose.usecase

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import me.proton.core.domain.entity.UserId
import me.proton.core.notification.domain.entity.NotificationPayload
import me.proton.core.notification.domain.usecase.ShowNotificationView
import me.proton.core.userrecovery.presentation.compose.DeviceRecoveryDeeplink
import me.proton.core.userrecovery.presentation.compose.R
import javax.inject.Inject

class ShowDeviceRecoveryNotification @Inject constructor(
    @ApplicationContext private val context: Context,
    private val showNotificationView: ShowNotificationView
) {
    operator fun invoke(userId: UserId) {
        val title = context.getString(R.string.user_recovery_notification_title)
        val body = context.getString(R.string.user_recovery_notification_body)
        val tag = "device-recovery-${userId.id}"
        showNotificationView.invoke(
            userId = userId,
            notificationId = tag.hashCode(),
            notificationTag = tag,
            payload = NotificationPayload.Unencrypted(
                raw = "",
                title = title,
                body = body
            ),
            contentDeeplink = DeviceRecoveryDeeplink.Recovery.get(userId),
            deleteDeeplink = null,
        )
    }
}
