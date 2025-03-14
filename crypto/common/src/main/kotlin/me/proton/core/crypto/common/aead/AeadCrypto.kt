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

package me.proton.core.crypto.common.aead

import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.PlainByteArray
import java.security.GeneralSecurityException

/**
 * Authenticated encryption with optional associated data.
 *
 * Default: AES/GCM/NoPadding, 12 bytes IV, 16 bytes GCM tag.
 *
 * Cryptographic interface providing [encrypt] function on [String] and [PlainByteArray],
 * and a [decrypt] function on [AeadEncryptedString] and [AeadEncryptedByteArray].
 *
 * @see KeyStoreCrypto for similar functions using KeyStore key.
 */
interface AeadCrypto {
    /**
     * Encrypt a [String] [value] using a given [key] and return an [AeadEncryptedString].
     *
     * @param key key used to encrypt [value]
     * @param aad additional associated data, while not encrypted, authenticated to ensure integrity
     *
     * @throws GeneralSecurityException if [value] cannot be encrypted.
     */
    fun encrypt(value: String, key: ByteArray, aad: ByteArray? = null): AeadEncryptedString

    /**
     * Decrypt an [AeadEncryptedString] [value] and return a [String].
     *
     * If [value] was encrypted with additional associated data, verification will fail if [aad] is not provided.
     *
     * @param key key used to decrypt [value]
     * @param aad additional associated data, while not decrypted, authenticated to ensure integrity
     *
     * @throws GeneralSecurityException if [value] cannot be decrypted.
     */
    fun decrypt(value: AeadEncryptedString, key: ByteArray, aad: ByteArray? = null): String

    /**
     * Encrypt a [PlainByteArray] [value] and return an [AeadEncryptedByteArray].
     *
     * @param key key used to encrypt [value]
     * @param aad additional associated data, while not encrypted, authenticated to ensure integrity
     *
     * @throws GeneralSecurityException if [value] cannot be encrypted.
     */
    fun encrypt(value: PlainByteArray, key: ByteArray, aad: ByteArray? = null): AeadEncryptedByteArray

    /**
     * Decrypt an [AeadEncryptedByteArray] [value] and return a [PlainByteArray].
     *
     * If [value] was encrypted with additional associated data, verification will fail if [aad] is not provided.
     *
     * @param key key used to decrypt [value]
     * @param aad additional associated data, while not decrypted, authenticated to ensure integrity
     *
     * @throws GeneralSecurityException if [value] cannot be decrypted.
     */
    fun decrypt(value: AeadEncryptedByteArray, key: ByteArray, aad: ByteArray? = null): PlainByteArray
}
