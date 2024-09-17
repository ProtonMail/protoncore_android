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

package me.proton.core.key.data.api.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.crypto.common.srp.Auth

@Serializable
data class SetupInitialKeysRequest(
    @SerialName("PrimaryKey")
    val primaryKey: String,
    @SerialName("KeySalt")
    val keySalt: String,
    @SerialName("OrgPrimaryUserKey")
    val orgPrimaryUserKey: String? = null,
    @SerialName("OrgActivationToken")
    val orgActivationToken: String? = null,
    @SerialName("AddressKeys")
    val addressKeys: List<CreateAddressKeyRequest>? = null,
    @SerialName("Auth")
    val auth: AuthRequest,
    @SerialName("EncryptedSecret")
    val encryptedSecret: String? = null
)

@Serializable
data class AuthRequest(
    @SerialName("Version")
    val version: Int,
    @SerialName("ModulusID")
    val modulusId: String,
    @SerialName("Salt")
    val salt: String,
    @SerialName("Verifier")
    val verifier: String
) {
    companion object {
        fun from(auth: Auth) = AuthRequest(
            version = auth.version,
            modulusId = auth.modulusId,
            salt = auth.salt,
            verifier = auth.verifier
        )
    }
}
