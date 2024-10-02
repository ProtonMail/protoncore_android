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

import me.proton.core.auth.domain.entity.AuthDeviceId
import me.proton.core.auth.domain.entity.DeviceSecretString
import me.proton.core.auth.domain.repository.AuthDeviceRepository
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.encrypt
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.decryptText
import me.proton.core.key.domain.useKeys
import me.proton.core.user.domain.repository.UserAddressRepository
import me.proton.core.util.kotlin.HashUtils
import javax.inject.Inject

class ValidateConfirmationCode @Inject constructor(
    private val userAddressRepository: UserAddressRepository,
    private val authDeviceRepository: AuthDeviceRepository,
    private val context: CryptoContext
) {

    sealed interface Result {
        data object NoDeviceSecret : Result
        data object Invalid : Result
        class Valid(val deviceSecret: DeviceSecretString) : Result
    }

    suspend operator fun invoke(
        userId: UserId,
        deviceId: AuthDeviceId,
        confirmationCode: String
    ): Result {
        if (confirmationCode.length != 4) return Result.Invalid
        val authDevice = authDeviceRepository.getByDeviceId(userId, deviceId) ?: return Result.NoDeviceSecret
        val activationToken = authDevice.activationToken ?: return Result.NoDeviceSecret
        val userAddressId = authDevice.addressId ?: return Result.NoDeviceSecret
        val userAddress = userAddressRepository.getAddress(userId, userAddressId) ?: return Result.NoDeviceSecret
        val decryptedDeviceSecret = userAddress.useKeys(context) { decryptText(activationToken) }
        val sha256DeviceSecret = HashUtils.sha256(decryptedDeviceSecret)
        val code = Crockford32.encode(sha256DeviceSecret.toByteArray()).take(4)
        return if (code == confirmationCode) {
            val deviceSecret = decryptedDeviceSecret.encrypt(context.keyStoreCrypto)
            Result.Valid(deviceSecret)
        } else {
            Result.Invalid
        }
    }
}
