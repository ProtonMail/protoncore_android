/*
 * Copyright (c) 2022 Proton Technologies AG
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

package me.proton.core.push.data.remote.resource

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.domain.entity.UserId
import me.proton.core.push.domain.entity.Push
import me.proton.core.push.domain.entity.PushId

@Serializable
internal data class PushResource(
    @SerialName("ID")
    val pushId: String,
    @SerialName("ObjectID")
    val objectId: String,
    @SerialName("Type")
    val type: String
)

internal fun PushResource.toPush(userId: UserId) = Push(
    userId,
    PushId(pushId),
    objectId,
    type
)
