/*
 * Copyright (c) 2020 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
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

package me.proton.core.key.domain.entity.key

import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.encryptWith
import me.proton.core.crypto.common.keystore.use
import me.proton.core.crypto.common.pgp.Armored
import me.proton.core.crypto.common.pgp.EncryptedMessage
import me.proton.core.crypto.common.pgp.PGPCrypto
import me.proton.core.crypto.common.pgp.Signature
import me.proton.core.key.domain.entity.keyholder.KeyHolder

/**
 * [PrivateKey] that can be unlocked by decrypting [passphrase] (verified with [passphraseSignature]).
 *
 * Typically used within nested [KeyHolder] (e.g. User Key -> UserAddress Key -> Calendar Key -> ...),
 * where you need another unlocked [PrivateKey] to decrypt [passphrase] and then be able to use the [PrivateKey], aso...
 */
data class NestedPrivateKey(
    val privateKey: PrivateKey,
    val passphrase: EncryptedMessage?,
    val passphraseSignature: Signature?
) {
    companion object {
        fun from(key: Armored, passphrase: EncryptedMessage, signature: Signature) = NestedPrivateKey(
            privateKey = PrivateKey(
                key = key,
                isPrimary = true,
                passphrase = null
            ),
            passphrase = passphrase,
            passphraseSignature = signature
        )

        /**
         * Generate a new [NestedPrivateKey].
         */
        fun generateNestedPrivateKey(
            context: CryptoContext,
            username: String,
            domain: String
        ): NestedPrivateKey {
            return context.pgpCrypto.generateNewToken().use { passphrase ->
                val privateKey = context.pgpCrypto.generateNewPrivateKey(
                    username = username,
                    domain = domain,
                    passphrase = passphrase.array,
                    keyType = PGPCrypto.KeyType.RSA,
                    keySecurity = PGPCrypto.KeySecurity.HIGH
                )
                val encryptedPassphrase = passphrase.encryptWith(context.keyStoreCrypto)
                val keyHolderPrivateKey = PrivateKey(
                    key = privateKey,
                    isPrimary = true,
                    passphrase = encryptedPassphrase
                )
                NestedPrivateKey(
                    privateKey = keyHolderPrivateKey,
                    passphrase = null,
                    passphraseSignature = null
                )
            }
        }
    }
}
