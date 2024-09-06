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

package me.proton.core.auth.data.usecase

import android.util.Base64
import me.proton.core.auth.domain.usecase.sso.GenerateDeviceSecret
import me.proton.core.crypto.common.context.CryptoContext
import javax.inject.Inject

/**
 * Generate new random device secret.
 *
 * @return 32-byte, base64-ed random salt as String, without newline character.
 */
class GenerateDeviceSecretImpl @Inject constructor(
    private val cryptoContext: CryptoContext
): GenerateDeviceSecret {

    override fun invoke(): String {
        val salt = cryptoContext.pgpCrypto.generateRandomBytes()
        val deviceSecret = Base64.encodeToString(salt, Base64.DEFAULT)
        // Truncate newline character.
        return deviceSecret.substring(0, deviceSecret.length - 1)
    }
}