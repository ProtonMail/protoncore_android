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

import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.domain.entity.UserId
import me.proton.core.key.data.api.response.UserKeyResponse
import me.proton.core.key.data.api.response.UserResponse
import me.proton.core.user.domain.entity.Role

object TestUsers {
    object User1 {
        val id = UserId("user1")
        val response = UserResponse(
            id = id.id,
            name = "userName1",
            usedSpace = 1000,
            currency = "CHF",
            credit = 100,
            maxSpace = 2000,
            maxUpload = 1000,
            role = Role.NoOrganization.value,
            private = 1,
            subscribed = 1,
            services = 1,
            delinquent = 0,
            email = "user1@example.com",
            displayName = "user 1 name",
            keys = listOf(Key1.response)
        )

        object Key1 {
            val passphrase = EncryptedByteArray(TestKeys.Key1.passphrase)
            val response = UserKeyResponse(
                id = "userKey1",
                version = 1,
                privateKey = TestKeys.Key1.privateKey,
                fingerprint = null,
                activation = null,
                primary = 1
            )
        }
    }

    object User2 {
        val id = UserId("user2")
        val response = UserResponse(
            id = id.id,
            name = "userName1",
            usedSpace = 1000,
            currency = "CHF",
            credit = 100,
            maxSpace = 2000,
            maxUpload = 1000,
            role = Role.NoOrganization.value,
            private = 1,
            subscribed = 1,
            services = 1,
            delinquent = 0,
            email = "user1@example.com",
            displayName = "user 1 name",
            keys = listOf(Key1.response)
        )

        object Key1 {
            val passphrase = EncryptedByteArray(TestKeys.Key2.passphrase)
            val response = UserKeyResponse(
                id = "user2Key1",
                version = 1,
                privateKey = TestKeys.Key2.privateKey,
                fingerprint = null,
                activation = null,
                primary = 1
            )
        }
    }
}
