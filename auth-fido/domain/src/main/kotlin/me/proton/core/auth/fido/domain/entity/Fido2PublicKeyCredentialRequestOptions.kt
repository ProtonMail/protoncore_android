/*
 * Copyright (c) 2024 Proton Technologies AG
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

package me.proton.core.auth.fido.domain.entity

import kotlinx.serialization.Serializable

@OptIn(ExperimentalUnsignedTypes::class)
@Serializable
public data class Fido2PublicKeyCredentialRequestOptions(
    val challenge: UByteArray,
    val timeout: ULong? = null, // milliseconds
    val rpId: String? = null,
    val allowCredentials: List<Fido2PublicKeyCredentialDescriptor>? = null,
    val userVerification: String? = null,
    val extensions: Fido2AuthenticationExtensionsClientInputs? = null
) {
    public fun hasExtensions(): Boolean =
        extensions?.appId != null || extensions?.thirdPartyPayment != null || extensions?.uvm != null
}
