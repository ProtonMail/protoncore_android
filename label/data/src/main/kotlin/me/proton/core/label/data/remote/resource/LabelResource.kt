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

package me.proton.core.label.data.remote.resource

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.domain.entity.UserId
import me.proton.core.label.domain.entity.Label
import me.proton.core.label.domain.entity.LabelId
import me.proton.core.label.domain.entity.LabelType
import me.proton.core.util.kotlin.toBooleanOrFalse

@Serializable
data class LabelResource(
    @SerialName("ID")
    val id: String,
    @SerialName("ParentID")
    val parentId: String? = null,
    @SerialName("Name")
    val name: String, // Max Length 100.
    @SerialName("Path")
    val path: String,
    @SerialName("Type")
    val type: Int,
    @SerialName("Color")
    val color: String,
    @SerialName("Order")
    val order: Int,
    @SerialName("Notify")
    val notify: Int? = null,
    @SerialName("Expanded")
    val expanded: Int? = null,
    @SerialName("Sticky")
    val sticky: Int? = null,
) {
    fun toLabel(userId: UserId) = Label(
        userId = userId,
        labelId = LabelId(id),
        parentId = parentId?.let { LabelId(it) },
        name = name,
        type = requireNotNull(LabelType.map[type]),
        path = path,
        color = color,
        order = order,
        isNotified = notify?.toBooleanOrFalse(),
        isExpanded = expanded?.toBooleanOrFalse(),
        isSticky = sticky?.toBooleanOrFalse()
    )
}
