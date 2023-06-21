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

package me.proton.core.notification.presentation.usecase

import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import me.proton.core.notification.presentation.R
import me.proton.core.notification.domain.usecase.ConfigureNotificationChannel
import javax.inject.Inject

private const val CHANNEL_IMPORTANCE = NotificationManagerCompat.IMPORTANCE_HIGH

public class ConfigureNotificationChannelImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ConfigureNotificationChannel {
    override fun invoke(channelId: String) {
        val notificationManager = NotificationManagerCompat.from(context)
        val channel = NotificationChannelCompat.Builder(channelId, CHANNEL_IMPORTANCE)
            .setName(context.getString(R.string.core_notification_account_channel_name))
            .build()
        notificationManager.createNotificationChannel(channel)
    }
}
