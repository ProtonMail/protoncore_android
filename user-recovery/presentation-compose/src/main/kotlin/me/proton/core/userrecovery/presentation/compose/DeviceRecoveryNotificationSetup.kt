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

package me.proton.core.userrecovery.presentation.compose

import me.proton.core.notification.presentation.deeplink.DeeplinkManager
import me.proton.core.userrecovery.domain.IsDeviceRecoveryEnabled
import me.proton.core.userrecovery.presentation.compose.DeviceRecoveryDialogActivity.DeviceRecoveryDialogInput
import javax.inject.Inject

class DeviceRecoveryNotificationSetup @Inject internal constructor(
    private val isDeviceRecoveryEnabled: IsDeviceRecoveryEnabled,
    private val deeplinkManager: DeeplinkManager
) {

    fun init() {
        if (!isDeviceRecoveryEnabled(userId = null)) return

        deeplinkManager.register(deeplink) { link ->
            val userId = link.args[0]
            DeviceRecoveryDialogActivity.start(requireNotNull(link.context), DeviceRecoveryDialogInput(userId))
            true
        }
    }

    companion object {
        const val deeplink = DeviceRecoveryDeeplink.Recovery.Deeplink
    }
}
