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

package me.proton.core.user.data

import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.decryptWith
import me.proton.core.crypto.common.keystore.encryptWith
import me.proton.core.crypto.common.keystore.use
import me.proton.core.crypto.common.pgp.Armored
import me.proton.core.crypto.common.pgp.PGPCrypto
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.decryptAndVerifyNestedKey
import me.proton.core.key.domain.encryptData
import me.proton.core.key.domain.entity.key.KeyId
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.key.domain.signData
import me.proton.core.key.domain.useKeys
import me.proton.core.user.data.entity.AddressKeyEntity
import me.proton.core.user.domain.entity.UserAddress
import me.proton.core.user.domain.entity.UserAddressKey
import me.proton.core.user.domain.entity.emailSplit
import me.proton.core.user.domain.repository.PassphraseRepository
import me.proton.core.user.domain.repository.UserRepository

/**
 * Provide User Address secret according old vs new format.
 */
class UserAddressKeySecretProvider(
    private val userRepository: UserRepository,
    private val passphraseRepository: PassphraseRepository,
    private val cryptoContext: CryptoContext,
) {
    private val keyStoreCrypto = cryptoContext.keyStoreCrypto

    suspend fun getPassphrase(userId: UserId, key: AddressKeyEntity): EncryptedByteArray? {
        return if (key.token == null || key.signature == null) {
            // Old address key format -> user passphrase.
            passphraseRepository.getPassphrase(userId)
        } else {
            // New address key format -> user keys encrypt token + signature -> address passphrase.
            userRepository.getUser(userId).useKeys(cryptoContext) {
                val decryptedKey = decryptAndVerifyNestedKey(
                    key = key.privateKey,
                    passphrase = key.token,
                    signature = key.signature
                )
                decryptedKey.privateKey.passphrase
            }
        }
    }

    data class UserAddressKeySecret(
        val passphrase: EncryptedByteArray,
        val token: Armored? = null,
        val signature: Armored? = null
    )

    private fun generateUserAddressKeySecret(
        userPrivateKey: PrivateKey,
        generateOldFormat: Boolean
    ): UserAddressKeySecret {
        return if (generateOldFormat) {
            // Old address key format -> user passphrase.
            UserAddressKeySecret(
                passphrase = checkNotNull(userPrivateKey.passphrase) { "Passphrase cannot be null." },
                token = null,
                signature = null
            )
        } else {
            // New address key format -> user keys encrypt token + signature -> address passphrase.
            cryptoContext.pgpCrypto.generateNewToken().use { passphrase ->
                UserAddressKeySecret(
                    passphrase = passphrase.encryptWith(keyStoreCrypto),
                    token = userPrivateKey.encryptData(cryptoContext, passphrase.array),
                    signature = userPrivateKey.signData(cryptoContext, passphrase.array)
                )
            }
        }
    }

    @Suppress("LongParameterList")
    fun generateUserAddressKey(
        generateOldFormat: Boolean,
        userAddress: UserAddress,
        userPrivateKey: PrivateKey,
        isPrimary: Boolean
    ): UserAddressKey {
        val secret = generateUserAddressKeySecret(userPrivateKey, generateOldFormat)
        secret.passphrase.decryptWith(keyStoreCrypto).use { decryptedPassphrase ->
            val email = userAddress.emailSplit
            val privateKey = PrivateKey(
                key = cryptoContext.pgpCrypto.generateNewPrivateKey(
                    username = email.username,
                    domain = email.domain,
                    passphrase = decryptedPassphrase.array,
                    keyType = PGPCrypto.KeyType.RSA,
                    keySecurity = PGPCrypto.KeySecurity.HIGH
                ),
                isPrimary = isPrimary,
                passphrase = secret.passphrase
            )
            return UserAddressKey(
                addressId = userAddress.addressId,
                version = 0,
                flags = 0,
                token = secret.token,
                signature = secret.signature,
                active = false,
                keyId = KeyId("temp"),
                privateKey = privateKey
            )
        }
    }
}
