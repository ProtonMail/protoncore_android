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

package me.proton.core.auth.data.api.fido2

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import me.proton.core.account.domain.entity.Fido2AuthenticationExtensionsClientInputs
import me.proton.core.account.domain.entity.Fido2PublicKeyCredentialRequestOptions

/**
 * Defined by [PublicKeyCredentialRequestOptions](https://www.w3.org/TR/webauthn-2/#dictionary-assertion-options).
 */
@OptIn(ExperimentalUnsignedTypes::class)
@Serializable
data class PublicKeyCredentialRequestOptionsResponse(
    val challenge: UByteArray,
    val timeout: ULong? = null, // milliseconds
    val rpId: String? = null,
    val allowCredentials: List<PublicKeyCredentialDescriptorData>? = null,
    val userVerification: String? = null,
    val extensions: JsonObject? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PublicKeyCredentialRequestOptionsResponse

        if (!challenge.contentEquals(other.challenge)) return false
        if (timeout != other.timeout) return false
        if (rpId != other.rpId) return false
        if (allowCredentials != other.allowCredentials) return false
        if (userVerification != other.userVerification) return false
        if (extensions != other.extensions) return false

        return true
    }

    override fun hashCode(): Int {
        var result = challenge.contentHashCode()
        result = 31 * result + (timeout?.hashCode() ?: 0)
        result = 31 * result + (rpId?.hashCode() ?: 0)
        result = 31 * result + (allowCredentials?.hashCode() ?: 0)
        result = 31 * result + (userVerification?.hashCode() ?: 0)
        result = 31 * result + (extensions?.hashCode() ?: 0)
        return result
    }

    fun toFido2PublicKeyCredentialRequestOptions() = Fido2PublicKeyCredentialRequestOptions(
        challenge = challenge,
        timeout = timeout,
        rpId = rpId,
        allowCredentials = allowCredentials?.map { it.toFido2PublicKeyCredentialDescriptor() },
        userVerification = userVerification,
        extensions = Fido2AuthenticationExtensionsClientInputs(
            appId = extensions?.get("appid")?.let { jsonElement ->
                (jsonElement as? JsonPrimitive)?.takeIf { it.isString }?.content?.takeIf { it.isNotEmpty() }
            },
            thirdPartyPayment = extensions?.get("thirdPartyPayment")?.let { jsonElement ->
                (jsonElement as? JsonPrimitive)?.booleanOrNull
            },
            uvm = extensions?.get("uvm")?.let { jsonElement ->
                (jsonElement as? JsonPrimitive)?.booleanOrNull
            },
        )
    )
}
