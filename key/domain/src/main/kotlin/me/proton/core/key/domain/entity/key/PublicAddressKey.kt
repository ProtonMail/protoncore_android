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
    val publicKey: PublicKey,
    val source: PublicAddressKeySource? = null
)

fun PublicAddressKey.canEncrypt(): Boolean = flags.canEncrypt()
fun PublicAddressKey.canVerify(): Boolean = flags.canVerify()

fun PublicAddressKey.canEncryptEmail(): Boolean = canEncrypt() && !flags.emailNoEncrypt()
fun PublicAddressKey.canVerifyEmail(): Boolean = canVerify() && !flags.emailNoSign()

fun List<PublicAddressKey>.canEncryptEmail() = any { it.canEncryptEmail() }
fun List<PublicAddressKey>.canVerifyEmail() = any { it.canVerifyEmail() }


/** Bitmap with the values from [KeyFlags]. */
typealias PublicAddressKeyFlags = Int

fun PublicAddressKeyFlags.isCompromised() = this.and(KeyFlags.NotCompromised) == 0
fun PublicAddressKeyFlags.isObsolete() = this.and(KeyFlags.NotObsolete) == 0

fun PublicAddressKeyFlags.canVerify(): Boolean = !isCompromised()
fun PublicAddressKeyFlags.canEncrypt(): Boolean = !isObsolete()

fun PublicAddressKeyFlags.emailNoEncrypt() = this.and(KeyFlags.EmailNoEncrypt) == KeyFlags.EmailNoEncrypt
fun PublicAddressKeyFlags.emailNoSign() = this.and(KeyFlags.EmailNoSign) == KeyFlags.EmailNoSign

