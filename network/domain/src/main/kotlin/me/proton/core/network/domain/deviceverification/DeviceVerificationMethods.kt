/*
 * Copyright (c) 2023 Proton Technologies AG
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

package me.proton.core.network.domain.deviceverification

import me.proton.core.domain.type.IntEnum

/**
 * A data class that represents the available device verification methods.
 *
 * @property challengeType an integer that represents the type of challenge.
 * @property challengePayload a string that contains the challenge payload.
 */
data class DeviceVerificationMethods(
    val challengeType: IntEnum<ChallengeType>,
    val challengePayload: String
)

public enum class ChallengeType(public val value: Int) {
    WASM(1),
    Argon2(2),
    Ecdlp(3);
    companion object {
        val map = values().associateBy { it.value }
        fun enumOf(value: Int) = IntEnum(value, map[value])
    }
}
