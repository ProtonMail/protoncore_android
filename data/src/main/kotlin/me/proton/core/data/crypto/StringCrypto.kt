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

package me.proton.core.data.crypto

/**
 * This interface provides an [encrypt] function on [String] and a [decrypt] function on [EncryptedString].
 */
interface StringCrypto {
    /**
     * Encrypt a [String] [value] and return an [EncryptedString].
     */
    fun encrypt(value: String): EncryptedString

    /**
     * Decrypt an [EncryptedString] [value] and return a [String].
     */
    fun decrypt(value: EncryptedString): String
}

/**
 * Decrypt an [EncryptedString] using a [StringCrypto].
 */
fun EncryptedString.decrypt(stringCrypto: StringCrypto) = stringCrypto.decrypt(this)

/**
 * Encrypt a [String] using a [StringCrypto].
 */
fun String.encrypt(stringCrypto: StringCrypto) = stringCrypto.encrypt(this)
