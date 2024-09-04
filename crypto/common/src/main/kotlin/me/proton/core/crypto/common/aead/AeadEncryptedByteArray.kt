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

package me.proton.core.crypto.common.aead

import me.proton.core.crypto.common.keystore.PlainByteArray

/**
 * Encrypted [ByteArray] provided by [AeadCrypto.encrypt].
 */
data class AeadEncryptedByteArray(val array: ByteArray) {

    override fun equals(other: Any?): Boolean =
        this === other || other is AeadEncryptedByteArray && array.contentEquals(other.array)

    override fun hashCode(): Int = array.contentHashCode()
}

/**
 * Decrypt an [AeadEncryptedByteArray] using a [AeadCrypto].
 *
 * @see AeadCrypto.decrypt
 */
fun AeadEncryptedByteArray.decrypt(crypto: AeadCrypto, key: ByteArray, aad: ByteArray? = null) =
    crypto.decrypt(value = this, key = key, aad = aad)

/**
 * Encrypt a [PlainByteArray] using a [AeadCrypto].
 *
 * @see AeadCrypto.encrypt
 */
fun PlainByteArray.encrypt(crypto: AeadCrypto, key: ByteArray, aad: ByteArray? = null) =
    crypto.encrypt(value = this, key = key, aad = aad)
