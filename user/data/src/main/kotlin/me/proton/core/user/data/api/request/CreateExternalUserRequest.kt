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

package me.proton.core.user.data.api.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.key.data.api.request.AuthRequest
import me.proton.core.user.domain.entity.NewExternalEmailUser

@Serializable
data class CreateExternalUserRequest(
    @SerialName("Email")
    val email: String,
    @SerialName("Referrer")
    val referrer: String? = null,
    @SerialName("Type")
    val type: Int,
    @SerialName("Auth")
    val auth: AuthRequest,
    @SerialName("Payload")
    val payload: PayloadRequest? // payload is from fingerprinting library. Android attestation + other stuff
) {
    companion object {
        fun from(newUser: NewExternalEmailUser): CreateExternalUserRequest =
            CreateExternalUserRequest(
                email = newUser.email,
                referrer = newUser.referrer,
                type = newUser.type,
                auth = AuthRequest.from(newUser.auth),
                payload = newUser.payload?.let {
                    PayloadRequest(it.fingerprint)
                }  // todo: in future add more fingerprint ids when fingerprinting module is done
            )
    }
}
