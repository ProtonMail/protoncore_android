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

package me.proton.core.auth.data.api.request

import android.util.Base64
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.auth.data.api.fido2.AuthenticationOptionsData
import me.proton.core.auth.data.api.fido2.PublicKeyCredentialDescriptorData
import me.proton.core.auth.data.api.fido2.PublicKeyCredentialRequestOptionsResponse
import me.proton.core.auth.fido.domain.entity.SecondFactorFido
import me.proton.core.auth.fido.domain.ext.toJson

@Serializable
data class SecondFactorRequest(
    @SerialName("TwoFactorCode")
    val secondFactorCode: String? = null,
    @SerialName("U2F")
    val universalTwoFactorRequest: UniversalTwoFactorRequest? = null,
    @SerialName("FIDO2")
    val fido2: Fido2Request? = null
)

@Serializable
data class UniversalTwoFactorRequest(
    @SerialName("KeyHandle")
    val keyHandle: String,
    @SerialName("ClientData")
    val clientData: String,
    @SerialName("SignatureData")
    val signatureData: String
)

@OptIn(ExperimentalUnsignedTypes::class)
@Serializable
data class Fido2Request(
    @SerialName("AuthenticationOptions")
    val authenticationOptions: AuthenticationOptionsData,
    @SerialName("ClientData")
    val clientData: String, // base64
    @SerialName("AuthenticatorData")
    val authenticatorData: String, // base64
    @SerialName("Signature")
    val signature: String, // base64
    @SerialName("CredentialID")
    val credentialID: UByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Fido2Request

        if (authenticationOptions != other.authenticationOptions) return false
        if (clientData != other.clientData) return false
        if (authenticatorData != other.authenticatorData) return false
        if (signature != other.signature) return false
        if (credentialID != other.credentialID) return false

        return true
    }

    override fun hashCode(): Int {
        var result = authenticationOptions.hashCode()
        result = 31 * result + clientData.hashCode()
        result = 31 * result + authenticatorData.hashCode()
        result = 31 * result + signature.hashCode()
        result = 31 * result + credentialID.hashCode()
        return result
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
fun SecondFactorFido.toFido2Request(): Fido2Request {
    val optionsData = AuthenticationOptionsData(
        PublicKeyCredentialRequestOptionsResponse(
            challenge = publicKeyOptions.challenge,
            timeout = publicKeyOptions.timeout,
            rpId = publicKeyOptions.rpId,
            allowCredentials = publicKeyOptions.allowCredentials?.map {
                PublicKeyCredentialDescriptorData(
                    type = it.type,
                    id = it.id,
                    transports = it.transports
                )
            },
            userVerification = publicKeyOptions.userVerification,
            extensions = publicKeyOptions.extensions?.toJson()
        )
    )
    return Fido2Request(
        authenticationOptions = optionsData,
        clientData = clientData.toBase64(),
        authenticatorData = authenticatorData.toBase64(),
        signature = signature.toBase64(),
        credentialID = credentialID.toUByteArray()
    )
}

private fun ByteArray.toBase64(): String = Base64.encodeToString(this, Base64.NO_WRAP)
