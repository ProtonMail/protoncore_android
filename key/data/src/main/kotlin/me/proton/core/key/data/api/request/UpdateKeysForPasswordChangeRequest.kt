/*
 * Copyright (c) 2021 Proton Technologies AG
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

@Serializable
data class UpdateKeysForPasswordChangeRequest(
    @SerialName("KeySalt")
    val keySalt: String,
    @SerialName("ClientEphemeral")
    val clientEphemeral: String,
    @SerialName("ClientProof")
    val clientProof: String,
    @SerialName("SRPSession")
    val srpSession: String,
    @SerialName("TwoFactorCode")
    val twoFactorCode: String,
    @SerialName("Auth")
    val auth: AuthRequest? = null,
    @SerialName("Keys")
    val keys: List<PrivateKeyRequest>? = null,
    @SerialName("UserKeys")
    val userKeys: List<PrivateKeyRequest>? = null,
    @SerialName("OrganizationKey")
    val organizationKey: String?
)

@Serializable
data class PrivateKeyRequest(
    @SerialName("PrivateKey")
    val privateKey: String,
    @SerialName("ID")
    val id: String
)

