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

import me.proton.core.auth.domain.repository.DeviceSecretRepository
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.domain.entity.UserId
import me.proton.core.util.kotlin.HashUtils
import javax.inject.Inject

class ValidateConfirmationCode @Inject constructor(
    private val deviceSecretRepository: DeviceSecretRepository,
    private val context: CryptoContext
) {

    sealed interface Result {
        object NoDeviceSecret : Result
        object ConfirmationCodeInputError : Result
        object ConfirmationCodeInvalid : Result
        object ConfirmationCodeValid : Result
    }

    suspend operator fun invoke(
        userId: UserId,
        confirmationCode: String
    ): Result {
        if (confirmationCode.isEmpty() or (confirmationCode.length != 4)) return Result.ConfirmationCodeInputError
        val deviceSecret = deviceSecretRepository.getByUserId(userId) ?: return Result.NoDeviceSecret
        val decryptedDeviceSecret = context.keyStoreCrypto.decrypt(deviceSecret.secret)
        val sha256DeviceSecret = HashUtils.sha256(decryptedDeviceSecret)
        return if (encode(sha256DeviceSecret.toByteArray()).take(4) == confirmationCode) Result.ConfirmationCodeValid
        else Result.ConfirmationCodeInvalid
    }
}
