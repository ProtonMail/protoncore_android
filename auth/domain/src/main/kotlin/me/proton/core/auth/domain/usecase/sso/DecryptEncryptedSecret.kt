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
import me.proton.core.crypto.common.aead.AeadEncryptedString
import me.proton.core.crypto.common.aead.decrypt
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.core.crypto.common.keystore.encrypt
import me.proton.core.crypto.common.keystore.use
import me.proton.core.domain.entity.UserId
import javax.inject.Inject

class DecryptEncryptedSecret @Inject constructor(
    context: CryptoContext,
    private val deviceSecretRepository: DeviceSecretRepository
) {
    private val keyStoreCrypto = context.keyStoreCrypto
    private val aeadCrypto = context.aeadCrypto
    private val pgpCrypto = context.pgpCrypto

    suspend operator fun invoke(
        userId: UserId,
        encryptedSecret: AeadEncryptedString?,
    ): EncryptedByteArray? {
        if (encryptedSecret == null) return null
        val deviceSecret = deviceSecretRepository.getByUserId(userId)?.secret ?: return null
        return pgpCrypto.getBase64Decoded(deviceSecret.decrypt(keyStoreCrypto)).use { key ->
            val decryptedSecret = encryptedSecret.decrypt(aeadCrypto, key = key.array)
            pgpCrypto.getBase64Decoded(decryptedSecret).use { it.encrypt(keyStoreCrypto) }
        }
    }
}
