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

import me.proton.core.crypto.common.pgp.exception.CryptoException

@Deprecated("Deprecated on BE.")
data class PublicAddress(
    val email: String,
    val recipientType: Int,
    val mimeType: String?,
    val keys: List<PublicAddressKey>,
    val signedKeyList: PublicSignedKeyList?,
    val ignoreKT: Int?,
) {
    val primaryKey by lazy {
        keys.firstOrNull { it.publicKey.isPrimary } ?: throw CryptoException("No primary key available.")
    }

    val recipient by lazy { Recipient.map[recipientType] }
}

enum class Recipient(val value: Int) {
    Internal(1),
    External(2);

    companion object {
        val map = values().associateBy { it.value }
    }
}

/**
 * Returns `true` if the [PublicAddress] has a key that can encrypt an email, otherwise `false`.
 */
fun PublicAddress.canEncryptEmail() = keys.canEncryptEmail()

/**
 * Returns `true` if the [PublicAddress] has a key that can verify an email, otherwise `false`.
 */
fun PublicAddress.canVerifyEmail() = keys.canVerifyEmail()
