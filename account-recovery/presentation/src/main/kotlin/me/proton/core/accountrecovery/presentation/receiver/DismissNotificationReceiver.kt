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

package me.proton.core.accountrecovery.presentation.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import me.proton.core.accountrecovery.domain.AccountRecoveryNotificationManager
import me.proton.core.accountrecovery.domain.AccountRecoveryState
import me.proton.core.domain.entity.UserId
import javax.inject.Inject

@AndroidEntryPoint
public class DismissNotificationReceiver : BroadcastReceiver() {
    @Inject
    internal lateinit var notificationManager: AccountRecoveryNotificationManager

    private val Intent.userId: UserId
        get() = requireNotNull(getStringExtra(ARG_USER_ID)?.let { UserId(it) }) {
            "Missing UserId parameter."
        }

    override fun onReceive(context: Context, intent: Intent) {
        notificationManager.updateNotification(AccountRecoveryState.None, intent.userId)
    }

    internal companion object {
        private const val ARG_USER_ID = "ARG_USER_ID"

        operator fun invoke(
            context: Context,
            userId: UserId
        ): Intent = Intent(context, DismissNotificationReceiver::class.java).apply {
            putExtra(ARG_USER_ID, userId.id)
        }
    }
}
