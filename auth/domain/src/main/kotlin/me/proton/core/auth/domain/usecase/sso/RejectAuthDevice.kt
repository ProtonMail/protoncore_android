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

package me.proton.core.auth.domain.usecase.sso

import me.proton.core.auth.domain.entity.AuthDeviceId
import me.proton.core.auth.domain.repository.AuthDeviceRepository
import me.proton.core.domain.entity.UserId
import me.proton.core.eventmanager.domain.EventManagerConfig
import me.proton.core.eventmanager.domain.EventManagerProvider
import me.proton.core.eventmanager.domain.extension.suspend
import javax.inject.Inject

/**
 * Sets an inactive device as rejected.
 * For rejecting a device for a member user, use [RejectMemberDevice].
 * @see ActivateAuthDevice
 */
class RejectAuthDevice @Inject constructor(
    private val authDeviceRepository: AuthDeviceRepository,
    private val eventManagerProvider: EventManagerProvider,
) {
    suspend operator fun invoke(
        userId: UserId,
        deviceId: AuthDeviceId
    ) {
        // Force Event Loop to update Pushes/Notifications.
        eventManagerProvider.suspend(EventManagerConfig.Core(userId)) {
            authDeviceRepository.rejectAuthDevice(userId, deviceId)
        }
    }
}
