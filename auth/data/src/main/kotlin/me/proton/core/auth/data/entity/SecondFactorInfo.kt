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
import me.proton.core.auth.domain.entity.SecondFactor
import me.proton.core.auth.domain.entity.UniversalTwoFactor
import me.proton.core.auth.domain.entity.UniversalTwoFactorKey
import me.proton.core.util.kotlin.toBoolean

@Serializable
data class SecondFactorInfo(
    @SerialName("Enabled")
    val enabled: Int,
    @SerialName("U2F")
    val universalTwoFactor: UniversalTwoFactorInfo? = null
) {
    fun toSecondFactor() = SecondFactor(
        enabled = enabled.toBoolean(),
        universalTwoFactor = universalTwoFactor?.toUniversalTwoFactor()
    )
}

@Serializable
data class UniversalTwoFactorInfo(
    @SerialName("Challenge")
    val challenge: String,
    @SerialName("RegisteredKeys")
    val registeredKeys: List<UniversalTwoFactorKeyInfo>
) {
    fun toUniversalTwoFactor() = UniversalTwoFactor(
        challenge = challenge,
        registeredKeys = registeredKeys.map { it.toUniversalTwoFactorKey() }
    )
}

@Serializable
data class UniversalTwoFactorKeyInfo(
    @SerialName("Version")
    val version: String,
    @SerialName("KeyHandle")
    val keyHandle: String
) {
    fun toUniversalTwoFactorKey() = UniversalTwoFactorKey(
        version = version,
        keyHandle = keyHandle
    )
}
