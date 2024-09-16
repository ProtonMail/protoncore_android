/*
 * Copyright (c) 2022 Proton Technologies AG
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

package me.proton.core.key.domain

import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.pgp.Armored
import me.proton.core.crypto.common.pgp.exception.CryptoException
import me.proton.core.key.domain.entity.key.ArmoredKey
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.key.domain.entity.key.PublicKey

/**
 * Convert [Armored] data to [ArmoredKey] (Private or Public).
 *
 * @throws CryptoException if it's an invalid armored key.
 */
fun Armored.toArmoredKey(
    cryptoContext: CryptoContext,
    isPrimary: Boolean,
    isActive: Boolean,
    canEncrypt: Boolean,
    canVerify: Boolean,
    passphrase: EncryptedByteArray? = null
) = when {
    cryptoContext.pgpCrypto.isPrivateKey(this) -> ArmoredKey.Private(
        armored = this,
        key = toPrivateKey(
            isPrimary = isPrimary,
            isActive = isActive,
            canEncrypt = canEncrypt,
            canVerify = canVerify,
            passphrase = passphrase
        )
    )
    cryptoContext.pgpCrypto.isPublicKey(this) -> ArmoredKey.Public(
        armored = this,
        key = toPublicKey(
            isPrimary = isPrimary,
            isActive = isActive,
            canEncrypt = canEncrypt,
            canVerify = canVerify
        )
    )
    else -> throw CryptoException("Invalid Armored Key.")
}

fun Armored.toPublicKey(
    isPrimary: Boolean = true,
    isActive: Boolean = true,
    canEncrypt: Boolean = true,
    canVerify: Boolean = true
): PublicKey = PublicKey(
    key = this,
    canEncrypt = canEncrypt,
    canVerify = canVerify,
    isPrimary = isPrimary,
    isActive = isActive
)

fun Armored.toPrivateKey(
    isPrimary: Boolean = true,
    isActive: Boolean = true,
    canEncrypt: Boolean = true,
    canVerify: Boolean = true,
    passphrase: EncryptedByteArray? = null
): PrivateKey = PrivateKey(
    key = this,
    canEncrypt = canEncrypt,
    canVerify = canVerify,
    isPrimary = isPrimary,
    isActive = isActive,
    passphrase = passphrase
)
