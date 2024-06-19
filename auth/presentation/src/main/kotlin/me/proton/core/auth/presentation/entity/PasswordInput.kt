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

package me.proton.core.auth.presentation.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import me.proton.core.auth.fido.domain.entity.Fido2AuthenticationExtensionsClientInputs
import me.proton.core.auth.fido.domain.entity.Fido2PublicKeyCredentialDescriptor
import me.proton.core.auth.fido.domain.entity.Fido2PublicKeyCredentialRequestOptions
import me.proton.core.auth.fido.domain.entity.SecondFactorFido

@Parcelize
data class PasswordInput(
    val password: String
): Parcelable

@Parcelize
data class TwoFAInput(
    val twoFA: String? = null,
    val twoFAFido: TwoFaFido? = null
): Parcelable

@Parcelize
data class TwoFaFido(
    val publicKeyOptions: Fido2PublicKeyCredentialRequestOptionsParcelable,
    val clientData: ByteArray,
    val authenticatorData: ByteArray,
    val signature: ByteArray,
    val credentialID: ByteArray
): Parcelable

@Parcelize
data class Fido2PublicKeyCredentialRequestOptionsParcelable(
    val challenge: UByteArray,
    val timeout: ULong? = null, // milliseconds
    val rpId: String? = null,
    val allowCredentials: List<Fido2PublicKeyCredentialDescriptorParcelable>? = null,
    val userVerification: String? = null,
    val extensions: Fido2AuthenticationExtensionsClientInputsParcelable? = null
): Parcelable

@OptIn(ExperimentalUnsignedTypes::class)
fun Fido2PublicKeyCredentialRequestOptionsParcelable.fromParcelable() =
    Fido2PublicKeyCredentialRequestOptions(
        challenge = challenge,
        timeout = timeout,
        rpId = rpId,
        allowCredentials = allowCredentials?.map { it.fromParcelable() },
        userVerification = userVerification,
        extensions = extensions?.fromParcelable()
    )

@OptIn(ExperimentalUnsignedTypes::class)
@Parcelize
data class Fido2PublicKeyCredentialDescriptorParcelable(
    val type: String,
    val id: UByteArray,
    val transports: List<String>?
): Parcelable

@Parcelize
data class Fido2AuthenticationExtensionsClientInputsParcelable(
    val appId: String?,
    val thirdPartyPayment: Boolean?,
    val uvm: Boolean?,
): Parcelable

fun TwoFaFido.fromParcelable() =
    SecondFactorFido(
        publicKeyOptions = publicKeyOptions.fromParcelable(),
        clientData = clientData,
        authenticatorData = authenticatorData,
        signature = signature,
        credentialID = credentialID
    )

@OptIn(ExperimentalUnsignedTypes::class)
fun Fido2PublicKeyCredentialDescriptorParcelable.fromParcelable() =
    Fido2PublicKeyCredentialDescriptor(
        type = type,
        id = id,
        transports = transports
    )

fun Fido2AuthenticationExtensionsClientInputsParcelable.fromParcelable() =
    Fido2AuthenticationExtensionsClientInputs(
        appId = appId,
        thirdPartyPayment = thirdPartyPayment,
        uvm = uvm
    )

@OptIn(ExperimentalUnsignedTypes::class)
fun Fido2PublicKeyCredentialRequestOptions.toParcelable() = Fido2PublicKeyCredentialRequestOptionsParcelable(
        challenge = challenge,
        timeout = timeout,
        rpId = rpId,
        allowCredentials = allowCredentials?.map { it.toParcelable() },
        userVerification = userVerification,
        extensions = extensions?.toParcelable()
    )

@OptIn(ExperimentalUnsignedTypes::class)
fun Fido2PublicKeyCredentialDescriptor.toParcelable() = Fido2PublicKeyCredentialDescriptorParcelable(
    type = type,
    id = id,
    transports = transports
)

fun Fido2AuthenticationExtensionsClientInputs.toParcelable() = Fido2AuthenticationExtensionsClientInputsParcelable(
    appId = appId,
    thirdPartyPayment = thirdPartyPayment,
    uvm = uvm
)
