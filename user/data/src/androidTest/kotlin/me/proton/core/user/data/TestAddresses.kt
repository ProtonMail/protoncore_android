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

package me.proton.core.user.data

import me.proton.core.key.data.api.response.AddressKeyResponse
import me.proton.core.key.data.api.response.AddressResponse
import me.proton.core.key.domain.entity.key.KeyId
import me.proton.core.user.domain.entity.AddressId
import me.proton.core.user.domain.entity.AddressType

object TestAddresses {
    object User1 {
        object Address1 {
            val id = AddressId("user1Address1")

            object Key1 {
                val id = KeyId("user1AddressKey1")
                val response = AddressKeyResponse(
                    id = id.id,
                    version = 1,
                    flags = 1,
                    privateKey = TestKeys.Key1.privateKey,
                    token = null,
                    signature = null,
                    fingerprint = null,
                    fingerprints = null,
                    activation = null,
                    primary = 1,
                    active = 1
                )
            }

            val response = AddressResponse(
                id = id.id,
                domainId = null,
                email = "user1address1@example.com",
                send = 1,
                receive = 1,
                status = 1,
                type = AddressType.Alias.value,
                order = 0,
                displayName = "User1 Address1",
                signature = null,
                hasKeys = 1,
                keys = listOf(Key1.response)
            )
        }
    }

    object User2 {
        object Address1 {
            val id = AddressId("user2Address1")

            object Key1 {
                val id = KeyId("user2AddressKey1")
                val response = AddressKeyResponse(
                    id = id.id,
                    version = 1,
                    flags = 1,
                    privateKey = TestKeys.Key1.privateKey,
                    token = TestKeys.Key1.passphraseEncryptedWithKey2,
                    signature = TestKeys.Key1.passphraseSignedWithKey2,
                    fingerprint = null,
                    fingerprints = null,
                    activation = null,
                    primary = 1,
                    active = 1
                )
            }

            val response = AddressResponse(
                id = id.id,
                domainId = null,
                email = "user2address1@example.com",
                send = 1,
                receive = 1,
                status = 1,
                type = AddressType.Alias.value,
                order = 0,
                displayName = "User2 Address1",
                signature = null,
                hasKeys = 1,
                keys = listOf(Key1.response)
            )
        }
    }
}
