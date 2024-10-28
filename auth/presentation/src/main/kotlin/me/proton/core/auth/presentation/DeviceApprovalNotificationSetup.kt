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

package me.proton.core.auth.presentation

import me.proton.core.auth.presentation.ui.DeviceApprovalActivity
import me.proton.core.domain.entity.UserId
import me.proton.core.notification.presentation.NotificationDeeplink
import me.proton.core.notification.presentation.deeplink.DeeplinkManager
import javax.inject.Inject

class DeviceApprovalNotificationSetup @Inject constructor(
    private val deeplinkManager: DeeplinkManager
) {
    operator fun invoke() {
        deeplinkManager.register(deeplink) { link ->
            val userId = UserId(link.args[0])
            DeviceApprovalActivity.start(requireNotNull(link.context), userId = userId, memberUserId = null)
            true
        }
    }

    internal companion object {
        private const val TYPE = "auth_device"
        val deeplink = NotificationDeeplink.Open.Deeplink.replace("{type}", TYPE)
    }
}
