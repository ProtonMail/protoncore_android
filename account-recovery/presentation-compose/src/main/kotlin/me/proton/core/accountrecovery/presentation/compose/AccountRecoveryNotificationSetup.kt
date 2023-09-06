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

package me.proton.core.accountrecovery.presentation.compose

import me.proton.core.accountrecovery.presentation.compose.entity.AccountRecoveryDialogInput
import me.proton.core.accountrecovery.presentation.compose.ui.AccountRecoveryDialogActivity
import me.proton.core.notification.presentation.NotificationDeeplink
import me.proton.core.notification.presentation.deeplink.DeeplinkManager
import javax.inject.Inject

class AccountRecoveryNotificationSetup @Inject internal constructor(
    private val deeplinkManager: DeeplinkManager
) {

    operator fun invoke() {
        deeplinkManager.register(deeplink) { link ->
            val userId = link.args[0]
            AccountRecoveryDialogActivity.start(requireNotNull(link.context), AccountRecoveryDialogInput(userId))
            true
        }
    }

    companion object {
        const val type = "account_recovery"
        val deeplink = NotificationDeeplink.Open.Deeplink.replace("{type}", type)
    }
}
