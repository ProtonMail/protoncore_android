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

package me.proton.core.auth.domain.usecase

import me.proton.core.auth.domain.entity.AuthDevice
import me.proton.core.auth.domain.repository.AuthDeviceRepository
import me.proton.core.domain.entity.UserId
import javax.inject.Inject

class CheckOtherDevices @Inject constructor(
    private val authDeviceRepository: AuthDeviceRepository
) {
    suspend operator fun invoke(
        hasTemporaryPassword: Boolean,
        userId: UserId
    ): Result {
        val devices = authDeviceRepository.getByUserId(userId)
        return when {
            devices.isNotEmpty() -> Result.OtherDevicesAvailable(devices)
            hasTemporaryPassword -> Result.AdminHelpRequired
            else -> Result.LoginWithBackupPasswordAvailable
        }
    }

    sealed interface Result {
        data object AdminHelpRequired : Result
        data object LoginWithBackupPasswordAvailable : Result

        /** Continue by waiting for authorized device, then try [AssociateAuthDevice] again. */
        data class OtherDevicesAvailable(val device: List<AuthDevice>) : Result
    }
}
