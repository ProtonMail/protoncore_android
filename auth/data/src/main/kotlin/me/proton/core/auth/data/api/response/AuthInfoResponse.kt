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

package me.proton.core.auth.data.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.auth.domain.entity.AuthInfo

@Suppress("LongParameterList")
@Serializable
data class AuthInfoResponse(
    @SerialName("Modulus")
    val modulus: String? = null,
    @SerialName("ServerEphemeral")
    val serverEphemeral: String? = null,
    @SerialName("Version")
    val version: Int? = null,
    @SerialName("Salt")
    val salt: String? = null,
    @SerialName("SRPSession")
    val srpSession: String? = null,
    @SerialName("2FA")
    val secondFactorInfo: SecondFactorInfoResponse? = null,
    @SerialName("SSOChallengeToken")
    val ssoChallengeToken: String? = null
) {
    fun toAuthInfo(username: String) = when {
        ssoChallengeToken != null -> AuthInfo.Sso(
            ssoChallengeToken = ssoChallengeToken
        )

        else -> AuthInfo.Srp(
            username = username,
            modulus = requireNotNull(modulus),
            serverEphemeral = requireNotNull(serverEphemeral),
            version = requireNotNull(version),
            salt = requireNotNull(salt),
            srpSession = requireNotNull(srpSession),
            secondFactor = secondFactorInfo?.toSecondFactor()
        )
    }
}
