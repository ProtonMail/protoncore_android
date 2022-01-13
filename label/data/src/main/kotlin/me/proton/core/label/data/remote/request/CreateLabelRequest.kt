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

package me.proton.core.label.data.remote.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.label.domain.entity.NewLabel
import me.proton.core.util.kotlin.toInt

@Serializable
data class CreateLabelRequest(
    @SerialName("ParentID")
    val parentId: String?,
    @SerialName("Name")
    val name: String,
    @SerialName("Type")
    val type: Int,
    @SerialName("Color")
    val color: String,
    @SerialName("Notify")
    val notify: Int?,
    @SerialName("Expanded")
    val expanded: Int?,
    @SerialName("Sticky")
    val sticky: Int?,
)

fun NewLabel.toCreateLabelRequest() = CreateLabelRequest(
    parentId = parentId?.id,
    name = name,
    type = type.value,
    color = color,
    notify = isNotified?.toInt(),
    expanded = isExpanded?.toInt(),
    sticky = isSticky?.toInt()
)
