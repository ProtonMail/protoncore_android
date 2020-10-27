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

package me.proton.core.auth.data.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.auth.domain.entity.FullAddressKey
import me.proton.core.util.kotlin.toBoolean

/**
 * @author Dino Kadrikj.
 */
@Serializable
data class FullAddressKeyEntity(
    @SerialName("ID")
    val id: String,
    @SerialName("Version")
    val version: Int,
    @SerialName("Flags")
    val flags: Int,
    @SerialName("PrivateKey")
    val privateKey: String,
    @SerialName("Token")
    val token: String?,
    @SerialName("Signature")
    val signature: String?,
    @SerialName("Fingerprint")
    val fingerprint: String,
    @SerialName("Fingerprints")
    val fingerprints: List<String>,
    @SerialName("Activation")
    val activation: String? = null,
    @SerialName("Primary")
    val primary: Int,
    @SerialName("Active")
    val active: Int // boolean (binary)
) {

    fun toAddressKey(): FullAddressKey =
        FullAddressKey(
            id = id,
            version = version,
            flags = flags,
            privateKey = privateKey,
            token = token,
            signature = signature,
            fingerprint = fingerprint,
            fingerprints = fingerprints,
            activation = activation,
            primary = primary.toBoolean(),
            active = active.toBoolean()
        )
}
