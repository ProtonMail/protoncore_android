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

package me.proton.core.user.data.usecase

import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.key.domain.entity.key.PublicSignedKeyList
import me.proton.core.key.domain.fingerprint
import me.proton.core.key.domain.signText
import me.proton.core.key.domain.useKeys
import me.proton.core.user.domain.entity.UserAddress
import me.proton.core.user.domain.entity.UserAddressKey
import javax.inject.Inject

class GenerateSignedKeyList @Inject constructor(
    private val cryptoContext: CryptoContext
) {

    operator fun invoke(userAddress: UserAddress): PublicSignedKeyList = with(userAddress) {
        keys
            .filter { it.active }
            .joinToString(",") { key ->
                "{" +
                    "\"Fingerprint\": \"${key.privateKey.fingerprint(cryptoContext)}\"," +
                    "\"SHA256Fingerprints\": ${key.jsonSHA256Fingerprints(cryptoContext)}," +
                    "\"Flags\": ${key.flags}," +
                    "\"Primary\": ${if (key.privateKey.isPrimary) "1" else "0"}" +
                    "}"
            }
            .let { keyList ->
                val keyListJSON = "[$keyList]"
                PublicSignedKeyList(
                    data = keyListJSON,
                    signature = useKeys(cryptoContext) { signText(keyListJSON) },
                    minEpochId = null,
                    maxEpochId = null,
                    expectedMinEpochId = null
                )
            }
    }
}

private fun UserAddressKey.jsonSHA256Fingerprints(context: CryptoContext) =
    context.pgpCrypto.getJsonSHA256Fingerprints(privateKey.key)
