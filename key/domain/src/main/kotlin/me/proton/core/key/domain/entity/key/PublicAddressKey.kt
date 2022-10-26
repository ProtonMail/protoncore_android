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

package me.proton.core.key.domain.entity.key

data class PublicAddressKey(
    val email: String,
    val flags: PublicAddressKeyFlags,
    val publicKey: PublicKey
)

fun PublicAddressKey.canEncrypt(): Boolean = flags.canEncrypt()
fun PublicAddressKey.canVerify(): Boolean = flags.canVerify()

fun PublicAddressKey.canEncryptEmail(): Boolean = canEncrypt() && !flags.emailNoEncrypt()
fun PublicAddressKey.canVerifyEmail(): Boolean = canVerify() && !flags.emailNoSign()

fun List<PublicAddressKey>.canEncryptEmail() = any { it.canEncryptEmail() }
fun List<PublicAddressKey>.canVerifyEmail() = any { it.canVerifyEmail() }


/**
 * Bitmap with the following values.
 *
 * - Key is not compromised = 1 (2^0) (if the bit is set to one the key is not compromised)
 * - Key is not obsolete = 2 (2^1)
 */
typealias PublicAddressKeyFlags = Int

private const val MASK_EMAIL_NO_ENCRYPT_VALUE = 4 // 100
private const val MASK_EMAIL_NO_SIGN_VALUE = 8 // 1000

fun PublicAddressKeyFlags.isCompromised() = this.and(1) == 0
fun PublicAddressKeyFlags.isObsolete() = this.and(2) == 0

fun PublicAddressKeyFlags.canVerify(): Boolean = !isCompromised()
fun PublicAddressKeyFlags.canEncrypt(): Boolean = !isObsolete()

fun PublicAddressKeyFlags.emailNoEncrypt() = this.and(MASK_EMAIL_NO_ENCRYPT_VALUE) == MASK_EMAIL_NO_ENCRYPT_VALUE
fun PublicAddressKeyFlags.emailNoSign() = this.and(MASK_EMAIL_NO_SIGN_VALUE) == MASK_EMAIL_NO_SIGN_VALUE

