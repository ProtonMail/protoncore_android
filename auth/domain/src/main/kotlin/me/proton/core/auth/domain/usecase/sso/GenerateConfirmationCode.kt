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

class GenerateConfirmationCode @Inject constructor(
    private val deviceSecretRepository: DeviceSecretRepository,
    private val context: CryptoContext
) {
    suspend operator fun invoke(
        userId: UserId
    ): String {
        val deviceSecret =
            deviceSecretRepository.getByUserId(userId) ?: throw IllegalStateException("Device Secret not found.")
        val decryptedDeviceSecret = context.keyStoreCrypto.decrypt(deviceSecret.secret)
        val sha256DeviceSecret = HashUtils.sha256(decryptedDeviceSecret)
        return encode(sha256DeviceSecret.toByteArray()).take(4)
    }
}