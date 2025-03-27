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
import me.proton.core.auth.domain.entity.RawSessionForkPayload
import me.proton.core.auth.domain.entity.SessionForkPayloadWithKey
import me.proton.core.crypto.common.aead.AeadCryptoFactory.Companion.DEFAULT_AES_GCM_CIPHER_TRANSFORMATION
import me.proton.core.crypto.common.aead.AeadCryptoFactory.Companion.DEFAULT_AES_KEY_ALGORITHM
import me.proton.core.crypto.common.aead.decrypt
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.PlainByteArray
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.core.crypto.common.keystore.encrypt
import me.proton.core.util.kotlin.DispatcherProvider
import me.proton.core.util.kotlin.deserialize
import javax.inject.Inject

class DecryptPassphrasePayload @Inject constructor(
    private val cryptoContext: CryptoContext,
    private val dispatcherProvider: DispatcherProvider,
) {
    /**
     * Decrypts the [payload].
     * @return A passphrase encrypted with [CryptoContext.keyStoreCrypto].
     */
    suspend operator fun invoke(
        payload: RawSessionForkPayload,
        encryptionKey: EncryptedByteArray,
        aesCipherGCMTagBits: Int,
        aesCipherIvBytes: Int
    ): EncryptedByteArray = withContext(dispatcherProvider.Comp) {
        val aeadCrypto = cryptoContext.aeadCryptoFactory.create(
            keyAlgorithm = DEFAULT_AES_KEY_ALGORITHM,
            transformation = DEFAULT_AES_GCM_CIPHER_TRANSFORMATION,
            authTagBits = aesCipherGCMTagBits,
            ivBytes = aesCipherIvBytes
        )
        val decryptedPayload = encryptionKey.decrypt(cryptoContext.keyStoreCrypto).use {
            payload.decrypt(aeadCrypto, key = it.array)
        }
        val payloadWithKey = decryptedPayload.deserialize<SessionForkPayloadWithKey>()
        val key = PlainByteArray(payloadWithKey.keyPassword.toByteArray())
        key.encrypt(cryptoContext.keyStoreCrypto)
    }
}
