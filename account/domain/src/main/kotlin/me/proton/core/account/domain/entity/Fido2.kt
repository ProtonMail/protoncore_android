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

package me.proton.core.account.domain.entity

import kotlinx.serialization.Serializable

@OptIn(ExperimentalUnsignedTypes::class)
@Serializable
data class Fido2RegisteredKey(
    val attestationFormat: String,
    val credentialID: UByteArray,
    val name: String
)

@Serializable
data class Fido2AuthenticationOptions(
    val publicKey: Fido2PublicKeyCredentialRequestOptions
)

@OptIn(ExperimentalUnsignedTypes::class)
@Serializable
data class Fido2PublicKeyCredentialRequestOptions(
    val challenge: UByteArray,
    val timeout: ULong?, // milliseconds
    val rpId: String?,
    val allowCredentials: List<Fido2PublicKeyCredentialDescriptor>? = null,
    val userVerification: String? = null,
    val extensions: Fido2AuthenticationExtensionsClientInputs? = null
)

@OptIn(ExperimentalUnsignedTypes::class)
@Serializable
data class Fido2PublicKeyCredentialDescriptor(
    val type: String,
    val id: UByteArray,
    val transports: List<String>?
)

/**
 * https://www.w3.org/TR/webauthn-2/#iface-authentication-extensions-client-inputs
 * https://www.w3.org/TR/webauthn-2/#sctn-defined-extensions
 * Extensions supported by FIDO2 API: https://developers.google.com/android/reference/com/google/android/gms/fido/fido2/api/common/AuthenticationExtensions.Builder
 */
@Serializable
data class Fido2AuthenticationExtensionsClientInputs(
    val appId: String?,
    val thirdPartyPayment: Boolean?,
    val uvm: Boolean?,
)
