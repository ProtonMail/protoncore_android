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

package me.proton.core.push.domain.entity

import kotlinx.serialization.Serializable
import me.proton.core.domain.entity.UserId

@Serializable
public data class PushId(val id: String)

@Serializable
public data class Push(
    val userId: UserId,
    val pushId: PushId,
    val objectId: String,
    val type: String,
)

@Serializable
public enum class PushObjectType(public val value: String) {
    Messages("Messages");

    public companion object {
        public val map: Map<String, PushObjectType> = values().associateBy { it.value }
    }
}
