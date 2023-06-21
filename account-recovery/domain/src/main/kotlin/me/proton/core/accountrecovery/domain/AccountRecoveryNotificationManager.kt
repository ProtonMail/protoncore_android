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

package me.proton.core.accountrecovery.domain

import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.UserRecovery
import javax.inject.Inject

public class AccountRecoveryNotificationManager @Inject constructor(
    private val cancelNotifications: CancelNotifications,
    private val configureAccountRecoveryChannel: ConfigureAccountRecoveryChannel,
    private val getAccountRecoveryChannelId: GetAccountRecoveryChannelId,
    private val isAccountRecoveryEnabled: IsAccountRecoveryEnabled,
    private val showNotification: ShowNotification
) {
    /** Sets up the notification channel for the use of account recovery process.
     * The setup is only performed if [isAccountRecoveryEnabled] returns `true`.
     */
    public fun setupNotificationChannel() {
        if (!isAccountRecoveryEnabled()) return
        val channelId = getAccountRecoveryChannelId()
        configureAccountRecoveryChannel(channelId = channelId)
    }

    /** Shows a notification for a given [forState].
     * Any previous notifications for account recovery for the [userId] are cancelled if:
     * - the [forState] is equal to [UserRecovery.State.None];
     * - the method [updateNotification] is called again.
     */
    public fun updateNotification(forState: UserRecovery.State, userId: UserId) {
        if (!isAccountRecoveryEnabled()) return
        if (forState == UserRecovery.State.None) {
            cancelNotifications(userId)
            return
        }

        showNotification(forState, userId)
    }
}
