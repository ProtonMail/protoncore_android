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

package me.proton.core.key.data.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.domain.entity.UserId
import me.proton.core.key.data.entity.KeySaltEntity
import me.proton.core.key.domain.entity.key.KeyId

@Serializable
data class KeySaltsResponse(
    @SerialName("KeySalts")
    val salts: List<KeySaltResponse>
) {
    fun toKeySaltEntityList(userId: UserId) = salts.map { it.toKeySalt(userId) }
}

@Serializable
data class KeySaltResponse(
    @SerialName("ID")
    val keyId: String,
    @SerialName("KeySalt")
    val keySalt: String? = null
) {
    fun toKeySalt(userId: UserId) = KeySaltEntity(
        userId = userId,
        keyId = KeyId(keyId),
        keySalt = keySalt
    )
}
