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

package me.proton.core.notification.presentation.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import me.proton.core.domain.entity.UserId
import me.proton.core.notification.domain.ProtonNotificationManager
import me.proton.core.notification.domain.entity.NotificationId
import me.proton.core.util.kotlin.CoroutineScopeProvider
import javax.inject.Inject

@AndroidEntryPoint
public class OnDismissNotificationReceiver : BroadcastReceiver() {
    @Inject
    internal lateinit var scopeProvider: CoroutineScopeProvider

    @Inject
    internal lateinit var notificationManager: ProtonNotificationManager

    private val Intent.notificationId: NotificationId
        get() = requireNotNull(getStringExtra(ARG_NOTIFICATION_ID)?.let { NotificationId(it) }) {
            "Missing notificationId parameter."
        }

    private val Intent.userId: UserId
        get() = requireNotNull(getStringExtra(ARG_USER_ID)?.let { UserId(it) }) {
            "Missing userId parameter."
        }

    override fun onReceive(context: Context, intent: Intent) {
        scopeProvider.GlobalDefaultSupervisedScope.launch {
            notificationManager.dismiss(intent.notificationId, intent.userId)
        }
    }

    internal companion object {
        private const val ARG_NOTIFICATION_ID = "ARG_NOTIFICATION_ID"
        private const val ARG_USER_ID = "ARG_USER_ID"

        operator fun invoke(
            context: Context,
            notificationId: NotificationId,
            userId: UserId
        ): Intent = Intent(context, OnDismissNotificationReceiver::class.java).apply {
            putExtra(ARG_NOTIFICATION_ID, notificationId.id)
            putExtra(ARG_USER_ID, userId.id)
        }
    }
}
