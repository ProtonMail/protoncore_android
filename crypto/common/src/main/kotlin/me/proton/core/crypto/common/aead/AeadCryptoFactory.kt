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

package me.proton.core.crypto.common.aead

interface AeadCryptoFactory {
    val default: AeadCrypto

    fun create(
        keyAlgorithm: String = DEFAULT_AES_KEY_ALGORITHM,
        transformation: String = DEFAULT_AES_GCM_CIPHER_TRANSFORMATION,
        authTagBits: Int = DEFAULT_AES_CIPHER_GCM_TAG_BITS,
        ivBytes: Int = DEFAULT_AES_CIPHER_IV_BYTES
    ): AeadCrypto

    companion object {
        const val DEFAULT_AES_KEY_ALGORITHM = "AES"
        const val DEFAULT_AES_GCM_CIPHER_TRANSFORMATION = "AES/GCM/NoPadding"
        const val DEFAULT_AES_CIPHER_GCM_TAG_BITS = 16 * 8
        const val DEFAULT_AES_CIPHER_IV_BYTES = 12
    }
}
