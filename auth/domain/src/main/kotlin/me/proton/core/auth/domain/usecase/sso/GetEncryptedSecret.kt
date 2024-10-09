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

import me.proton.core.auth.domain.entity.DeviceSecret
import me.proton.core.auth.domain.entity.DeviceSecretString
import me.proton.core.crypto.common.aead.encrypt
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.PlainByteArray
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.core.crypto.common.keystore.use
import me.proton.core.crypto.common.pgp.Based64Encoded
import javax.inject.Inject

/**
 * Get EncryptedSecret from Passphrase + DeviceSecret.
 *
 * ```
 * EncryptedSecret = base64Encode(aesGcm(data = passphrase, key = base64Decoded(deviceSecret), context))
 * Passphrase = bcrypt(password, salt)
 * Context = "account.device-secret"
 * ```
 * @see DecryptEncryptedSecret
 */
class GetEncryptedSecret @Inject constructor(
    context: CryptoContext
) {
    private val keyStoreCrypto = context.keyStoreCrypto
    private val aeadCrypto = context.aeadCrypto
    private val pgpCrypto = context.pgpCrypto

    operator fun invoke(
        passphrase: PlainByteArray,
        deviceSecret: DeviceSecretString
    ): Based64EncodedAeadEncryptedSecret = pgpCrypto.getBase64Encoded(
        pgpCrypto.getBase64Decoded(deviceSecret.decrypt(keyStoreCrypto)).use { key ->
            passphrase.encrypt(
                crypto = aeadCrypto,
                key = key.array,
                aad = DeviceSecret.DEVICE_SECRET_CONTEXT.toByteArray()
            ).array
        }
    )
}

internal typealias Based64EncodedAeadEncryptedSecret = Based64Encoded
