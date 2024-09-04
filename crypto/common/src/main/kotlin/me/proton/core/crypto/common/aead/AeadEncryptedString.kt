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

/**
 * Encrypted [String] provided by [AeadCrypto.encrypt].
 */
typealias AeadEncryptedString = String

/**
 * Decrypt an [AeadEncryptedString] using a [AeadCrypto].
 *
 * @see AeadCrypto.decrypt
 */
fun AeadEncryptedString.decrypt(crypto: AeadCrypto, key: ByteArray, aad: ByteArray? = null) =
    crypto.decrypt(value = this, key = key, aad = aad)

/**
 * Encrypt a [String] using a [AeadCrypto].
 *
 * @see AeadCrypto.encrypt
 */
fun String.encrypt(crypto: AeadCrypto, key: ByteArray, aad: ByteArray? = null) =
    crypto.encrypt(value = this, key = key, aad = aad)
