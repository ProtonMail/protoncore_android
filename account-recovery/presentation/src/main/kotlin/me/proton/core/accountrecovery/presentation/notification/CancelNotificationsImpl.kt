/*
 * Copyright (c) 2023 Proton AG
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

package me.proton.core.accountrecovery.presentation.notification

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import me.proton.core.accountrecovery.domain.CancelNotifications
import me.proton.core.accountrecovery.presentation.internal.GetNotificationId
import me.proton.core.accountrecovery.presentation.internal.GetNotificationTag
import me.proton.core.domain.entity.UserId
import javax.inject.Inject

public class CancelNotificationsImpl @Inject internal constructor(
    @ApplicationContext private val context: Context,
    private val getNotificationId: GetNotificationId,
    private val getNotificationTag: GetNotificationTag
) : CancelNotifications {
    override fun invoke(userId: UserId) {
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(getNotificationTag(userId), getNotificationId())
    }
}
