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

package me.proton.core.crypto.common.keystore

/**
 * Encrypted [ByteArray] provided by [KeyStoreCrypto.encrypt].
 */
data class EncryptedByteArray(val array: ByteArray) {

    override fun equals(other: Any?): Boolean =
        this === other || other is EncryptedByteArray && array.contentEquals(other.array)

    override fun hashCode(): Int = array.contentHashCode()
}

/**
 * Decrypt an [EncryptedByteArray] using a [KeyStoreCrypto].
 */
fun EncryptedByteArray.decrypt(crypto: KeyStoreCrypto) = crypto.decrypt(this)

/**
 * Encrypt a [PlainByteArray] using a [KeyStoreCrypto].
 */
fun PlainByteArray.encrypt(crypto: KeyStoreCrypto) = crypto.encrypt(this)

/**
 * Returns decrypted value, or the result of [onFailure] function on decryption failure.
 *
 * @see [EncryptedByteArray.decrypt]
 */
fun EncryptedByteArray.decryptOrElse(
    crypto: KeyStoreCrypto,
    onFailure: (Throwable) -> PlainByteArray?
) = crypto.decryptOrElse(this, onFailure)

/**
 * Returns encrypted value, or the result of [onFailure] function on encryption failure.
 *
 * @see [PlainByteArray.encrypt]
 */
fun PlainByteArray.encryptOrElse(
    crypto: KeyStoreCrypto,
    onFailure: (Throwable) -> EncryptedByteArray?
) = crypto.encryptOrElse(this, onFailure)
