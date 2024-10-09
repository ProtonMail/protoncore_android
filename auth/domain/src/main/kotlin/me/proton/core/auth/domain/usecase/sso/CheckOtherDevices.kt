/*
 * Copyright (c) 2024 Proton Technologies AG
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

import me.proton.core.auth.domain.entity.AuthDevice
import me.proton.core.auth.domain.entity.AuthDeviceState
import me.proton.core.auth.domain.entity.isPendingAdmin
import me.proton.core.auth.domain.repository.AuthDeviceRepository
import me.proton.core.auth.domain.repository.DeviceSecretRepository
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.extension.hasTemporaryPassword
import javax.inject.Inject

class CheckOtherDevices @Inject constructor(
    private val userManager: UserManager,
    private val authDeviceRepository: AuthDeviceRepository,
    private val deviceSecretRepository: DeviceSecretRepository,
) {
    suspend operator fun invoke(
        userId: UserId
    ): Result {
        val hasTemporaryPassword = userManager.getUser(userId).hasTemporaryPassword()
        val localDeviceId = requireNotNull(deviceSecretRepository.getByUserId(userId)?.deviceId)
        val localDevice = requireNotNull(authDeviceRepository.getByDeviceId(userId, localDeviceId))
        val devices = authDeviceRepository.getByUserId(userId)
        val activeDevices = devices.filter { it.state == AuthDeviceState.Active }
        return when {
            hasTemporaryPassword -> Result.AdminHelpRequired
            localDevice.isPendingAdmin() -> Result.AdminHelpRequested
            activeDevices.isNotEmpty() -> Result.OtherDevicesAvailable(devices)
            else -> Result.BackupPassword
        }
    }

    sealed interface Result {
        data object AdminHelpRequired : Result
        data object AdminHelpRequested : Result
        data object BackupPassword : Result

        /** Continue by waiting for authorized device, then try [AssociateAuthDevice] again. */
        data class OtherDevicesAvailable(val device: List<AuthDevice>) : Result
    }
}
