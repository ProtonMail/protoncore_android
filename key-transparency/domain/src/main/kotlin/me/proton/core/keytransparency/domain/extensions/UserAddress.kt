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

package me.proton.core.keytransparency.domain.extensions

import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.key.domain.entity.key.PublicAddress
import me.proton.core.key.domain.entity.key.PublicAddressKey
import me.proton.core.key.domain.entity.key.Recipient
import me.proton.core.key.domain.publicKey
import me.proton.core.user.domain.entity.UserAddress
import me.proton.core.user.domain.entity.UserAddressKey

internal fun UserAddress.toPublicAddress(cryptoContext: CryptoContext) = PublicAddress(
    email = email,
    recipientType = Recipient.Internal.value,
    mimeType = "",
    keys = keys.map { it.toPublicAddressKey(cryptoContext, email) },
    signedKeyList = signedKeyList,
    ignoreKT = null // no need to check the user public keys against KT
)

internal fun UserAddressKey.toPublicAddressKey(cryptoContext: CryptoContext, email: String) = PublicAddressKey(
    email = email,
    flags = flags,
    publicKey = privateKey.publicKey(cryptoContext)
)

internal fun UserAddressKey.jsonSHA256Fingerprints(context: CryptoContext) =
    context.pgpCrypto.getJsonSHA256Fingerprints(privateKey.key)
