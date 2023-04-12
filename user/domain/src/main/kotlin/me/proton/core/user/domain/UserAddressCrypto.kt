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

package me.proton.core.user.domain

import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.pgp.exception.CryptoException
import me.proton.core.key.domain.entity.key.NestedPrivateKey
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.key.domain.entity.key.PublicSignedKeyList
import me.proton.core.key.domain.fingerprint
import me.proton.core.key.domain.signText
import me.proton.core.key.domain.useKeys
import me.proton.core.user.domain.entity.UserAddress
import me.proton.core.user.domain.entity.UserAddressKey
import me.proton.core.user.domain.entity.emailSplit

/**
 * Generate new [NestedPrivateKey] from [UserAddress] username & domain.
 *
 * Note: Only this [UserAddress] will be able to decrypt.
 */
fun UserAddress.generateNestedPrivateKey(context: CryptoContext): NestedPrivateKey =
    emailSplit.let { NestedPrivateKey.generateNestedPrivateKey(context, it.username, it.domain) }

fun UserAddress.signKeyList(context: CryptoContext): PublicSignedKeyList = keys
    .filter { it.active }
    .joinToString(",") { key ->
        "{" +
            "\"Fingerprint\": \"${key.privateKey.fingerprint(context)}\"," +
            "\"SHA256Fingerprints\": ${key.jsonSHA256Fingerprints(context)}," +
            "\"Flags\": ${key.flags}," +
            "\"Primary\": ${if (key.privateKey.isPrimary) "1" else "0"}" +
        "}"
    }.let { keyList ->
        val keyListJSON = "[$keyList]"
        PublicSignedKeyList(
            data = keyListJSON,
            signature = useKeys(context) { signText(keyListJSON) },
            expectedMinEpochId = null,
            minEpochId = null,
            maxEpochId = null
        )
    }

/**
 * Get JSON SHA256 fingerprints from this [PrivateKey].
 *
 * @throws [CryptoException] if fingerprint cannot be extracted.
 */
internal fun UserAddressKey.jsonSHA256Fingerprints(context: CryptoContext) =
    context.pgpCrypto.getJsonSHA256Fingerprints(privateKey.key)
