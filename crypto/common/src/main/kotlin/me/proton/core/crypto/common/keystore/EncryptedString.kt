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
 * Encrypted [String] provided by [KeyStoreCrypto.encrypt].
 */
typealias EncryptedString = String

/**
 * Decrypt an [EncryptedString] using a [KeyStoreCrypto].
 */
fun EncryptedString.decrypt(crypto: KeyStoreCrypto) = crypto.decrypt(this)

/**
 * Encrypt a [String] using a [KeyStoreCrypto].
 */
fun String.encrypt(crypto: KeyStoreCrypto) = crypto.encrypt(this)

/**
 * Returns decrypted value, or the result of onFailure function on decryption failure.
 *
 * @see [EncryptedString.decryptWith]
 */
fun EncryptedString.decryptOrElse(
    crypto: KeyStoreCrypto,
    onFailure: (Throwable) -> String?
) = crypto.decryptOrElse(this, onFailure)

/**
 * Returns encrypted value, or the result of [onFailure] function on encryption failure.
 *
 * @see [String.encrypt]
 */
fun String.encryptOrElse(
    crypto: KeyStoreCrypto,
    onFailure: (Throwable) -> EncryptedString?
) = crypto.encryptOrElse(this, onFailure)
