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
 * KeyStore Cryptographic interface providing [encrypt] function on [String] and [PlainByteArray],
 * and a [decrypt] function on [EncryptedString] and [EncryptedByteArray].
 */
interface KeyStoreCrypto {
    /**
     * Encrypt a [String] [value] and return an [EncryptedString].
     */
    fun encrypt(value: String): EncryptedString

    /**
     * Decrypt an [EncryptedString] [value] and return a [String].
     */
    fun decrypt(value: EncryptedString): String

    /**
     * Encrypt a [PlainByteArray] [value] and return an [EncryptedByteArray].
     */
    fun encrypt(value: PlainByteArray): EncryptedByteArray

    /**
     * Decrypt an [EncryptedByteArray] [value] and return a [PlainByteArray].
     */
    fun decrypt(value: EncryptedByteArray): PlainByteArray
}
