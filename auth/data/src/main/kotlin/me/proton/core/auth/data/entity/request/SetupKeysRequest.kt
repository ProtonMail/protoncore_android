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

package me.proton.core.auth.data.entity.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.auth.domain.entity.AddressKey

/**
 * @author Dino Kadrikj.
 */
@Serializable
data class SetupKeysRequest(
    val primaryKey: String,
    val keySalt: String,
    val addressKeys: List<AddressKeyEntity>,
    val auth: AuthEntity
)

@Serializable
data class AuthEntity(
    @SerialName("Version")
    val version: Int,
    @SerialName("ModulusID")
    val modulusId: String,
    @SerialName("Salt")
    val salt: String,
    @SerialName("Verifier")
    val verifier: String
)

@Serializable
data class AddressKeyEntity(
    @SerialName("AddressID")
    val addressId: String,
    @SerialName("PrivateKey")
    val privateKey: String,
    @SerialName("SignedKeyList")
    val signedKeyList: SignedKeyList
) {
    companion object {
        fun fromAddressKeySetup(addressKey: AddressKey): AddressKeyEntity =
            AddressKeyEntity(
                addressId = addressKey.addressId,
                privateKey = addressKey.privateKey,
                signedKeyList = SignedKeyList(addressKey.signedKeyList.data, addressKey.signedKeyList.signature)
            )
    }
}
