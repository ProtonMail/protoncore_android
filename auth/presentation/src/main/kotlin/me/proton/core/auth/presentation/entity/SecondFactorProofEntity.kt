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

package me.proton.core.auth.presentation.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import me.proton.core.auth.fido.domain.entity.Fido2AuthenticationExtensionsClientInputs
import me.proton.core.auth.fido.domain.entity.Fido2PublicKeyCredentialDescriptor
import me.proton.core.auth.fido.domain.entity.Fido2PublicKeyCredentialRequestOptions
import me.proton.core.auth.fido.domain.entity.SecondFactorProof

sealed class SecondFactorProofEntity : Parcelable {

    @Parcelize
    data class SecondFactorCodeEntity(
        val code: String
    ) : SecondFactorProofEntity()

    @Parcelize
    data class SecondFactorSignatureEntity(
        val keyHandle: String,
        val clientData: String,
        val signatureData: String
    ) : SecondFactorProofEntity()

    @Parcelize
    class Fido2Entity(
        val publicKeyOptions: Fido2PublicKeyCredentialRequestOptionsEntity,
        val clientData: ByteArray,
        val authenticatorData: ByteArray,
        val signature: ByteArray,
        val credentialID: ByteArray
    ) : SecondFactorProofEntity()
}

@Parcelize
data class Fido2PublicKeyCredentialRequestOptionsEntity(
    val challenge: UByteArray,
    val timeout: ULong? = null, // milliseconds
    val rpId: String? = null,
    val allowCredentials: List<Fido2PublicKeyCredentialDescriptorEntity>? = null,
    val userVerification: String? = null,
    val extensions: Fido2AuthenticationExtensionsClientInputsEntity? = null
): Parcelable

@OptIn(ExperimentalUnsignedTypes::class)
@Parcelize
data class Fido2PublicKeyCredentialDescriptorEntity(
    val type: String,
    val id: UByteArray,
    val transports: List<String>?
): Parcelable

@Parcelize
data class Fido2AuthenticationExtensionsClientInputsEntity(
    val appId: String?,
    val thirdPartyPayment: Boolean?,
    val uvm: Boolean?,
): Parcelable

@OptIn(ExperimentalUnsignedTypes::class)
fun Fido2PublicKeyCredentialRequestOptionsEntity.fromEntity() =
    Fido2PublicKeyCredentialRequestOptions(
        challenge = challenge,
        timeout = timeout,
        rpId = rpId,
        allowCredentials = allowCredentials?.map { it.fromEntity() },
        userVerification = userVerification,
        extensions = extensions?.fromEntity()
    )

fun SecondFactorProofEntity.fromEntity() =
    when (this) {
        is SecondFactorProofEntity.SecondFactorCodeEntity -> SecondFactorProof.SecondFactorCode(code)
        is SecondFactorProofEntity.SecondFactorSignatureEntity -> SecondFactorProof.SecondFactorSignature(keyHandle, clientData, signatureData)
        is SecondFactorProofEntity.Fido2Entity -> fromEntity()
    }

fun SecondFactorProofEntity.Fido2Entity.fromEntity() =
    SecondFactorProof.Fido2(
        publicKeyOptions = publicKeyOptions.fromEntity(),
        clientData = clientData,
        authenticatorData = authenticatorData,
        signature = signature,
        credentialID = credentialID
    )

@OptIn(ExperimentalUnsignedTypes::class)
fun Fido2PublicKeyCredentialDescriptorEntity.fromEntity() =
    Fido2PublicKeyCredentialDescriptor(
        type = type,
        id = id,
        transports = transports
    )

fun Fido2AuthenticationExtensionsClientInputsEntity.fromEntity() =
    Fido2AuthenticationExtensionsClientInputs(
        appId = appId,
        thirdPartyPayment = thirdPartyPayment,
        uvm = uvm
    )

@OptIn(ExperimentalUnsignedTypes::class)
fun Fido2PublicKeyCredentialRequestOptions.toEntity() = Fido2PublicKeyCredentialRequestOptionsEntity(
    challenge = challenge,
    timeout = timeout,
    rpId = rpId,
    allowCredentials = allowCredentials?.map { it.toEntity() },
    userVerification = userVerification,
    extensions = extensions?.toEntity()
)

@OptIn(ExperimentalUnsignedTypes::class)
fun Fido2PublicKeyCredentialDescriptor.toEntity() = Fido2PublicKeyCredentialDescriptorEntity(
    type = type,
    id = id,
    transports = transports
)

fun Fido2AuthenticationExtensionsClientInputs.toEntity() = Fido2AuthenticationExtensionsClientInputsEntity(
    appId = appId,
    thirdPartyPayment = thirdPartyPayment,
    uvm = uvm
)
