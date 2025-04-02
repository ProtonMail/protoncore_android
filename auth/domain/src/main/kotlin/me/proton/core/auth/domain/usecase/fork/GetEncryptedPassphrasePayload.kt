/*
 * Copyright (c) 2025 Proton AG
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

package me.proton.core.auth.domain.usecase.fork

import kotlinx.coroutines.withContext
import me.proton.core.auth.domain.entity.SessionForkPayloadWithKey
import me.proton.core.crypto.common.aead.AeadCryptoFactory.Companion.DEFAULT_AES_GCM_CIPHER_TRANSFORMATION
import me.proton.core.crypto.common.aead.AeadCryptoFactory.Companion.DEFAULT_AES_KEY_ALGORITHM
import me.proton.core.crypto.common.aead.encrypt
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.repository.PassphraseRepository
import me.proton.core.util.kotlin.DispatcherProvider
import me.proton.core.util.kotlin.serialize
import javax.inject.Inject

class GetEncryptedPassphrasePayload @Inject constructor(
    private val cryptoContext: CryptoContext,
    private val dispatcherProvider: DispatcherProvider,
    private val passphraseRepository: PassphraseRepository,
) {
    /** Prepares a payload for using with [me.proton.core.auth.domain.usecase.ForkSession].
     * The payload contains the passphrase of the [user][userId],
     * wrapped in [SessionForkPayloadWithKey], serialized to JSON string,
     * and encrypted with [encryptionKey].
     */
    suspend operator fun invoke(
        userId: UserId,
        encryptionKey: EncryptedByteArray,
        aesCipherGCMTagBits: Int,
        aesCipherIvBytes: Int
    ): String = withContext(dispatcherProvider.Comp) {
        val aeadCrypto = cryptoContext.aeadCryptoFactory.create(
            keyAlgorithm = DEFAULT_AES_KEY_ALGORITHM,
            transformation = DEFAULT_AES_GCM_CIPHER_TRANSFORMATION,
            authTagBits = aesCipherGCMTagBits,
            ivBytes = aesCipherIvBytes
        )

        // Note: passphrase may be `null` on VPN.
        passphraseRepository.getPassphrase(userId)?.decrypt(cryptoContext.keyStoreCrypto).use { decryptedPassphrase ->
            val serializedPayload = SessionForkPayloadWithKey(
                keyPassword = decryptedPassphrase?.let { String(it.array) }
            ).serialize()
            encryptionKey.decrypt(cryptoContext.keyStoreCrypto).use { key ->
                serializedPayload.encrypt(aeadCrypto, key = key.array)
            }
        }
    }
}
