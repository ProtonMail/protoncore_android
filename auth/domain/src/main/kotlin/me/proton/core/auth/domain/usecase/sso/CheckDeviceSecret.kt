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

import me.proton.core.auth.domain.repository.DeviceSecretRepository
import me.proton.core.auth.domain.usecase.AssociateAuthDevice
import me.proton.core.crypto.common.aead.AeadEncryptedString
import me.proton.core.domain.entity.UserId
import javax.inject.Inject

/**
 * Check for a local valid DeviceSecret, remotely associate Device and return EncryptedSecret.
 */
class CheckDeviceSecret @Inject constructor(
    private val associateAuthDevice: AssociateAuthDevice,
    private val deviceSecretRepository: DeviceSecretRepository,
) {

    suspend operator fun invoke(
        userId: UserId
    ): AeadEncryptedString? {
        val deviceSecret = deviceSecretRepository.getByUserId(userId) ?: return null
        val associateResult = associateAuthDevice.invoke(
            userId = userId,
            deviceId = deviceSecret.deviceId,
            deviceToken = deviceSecret.token,
        )
        return when (associateResult) {
            is AssociateAuthDevice.Result.Error.DeviceNotActive -> null
            is AssociateAuthDevice.Result.Error.DeviceNotFound -> null
            is AssociateAuthDevice.Result.Error.DeviceRejected -> null
            is AssociateAuthDevice.Result.Error.DeviceTokenInvalid -> null
            is AssociateAuthDevice.Result.Error.SessionAlreadyAssociated -> null
            is AssociateAuthDevice.Result.Success -> associateResult.encryptedSecret
        }
    }
}
