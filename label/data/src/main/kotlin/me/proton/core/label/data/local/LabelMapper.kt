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

package me.proton.core.label.data.local

import me.proton.core.label.domain.entity.Label
import me.proton.core.label.domain.entity.LabelId
import me.proton.core.label.domain.entity.LabelType

fun Label.toEntity() = LabelEntity(
    userId = userId,
    labelId = labelId.id,
    parentId = parentId?.id,
    name = name,
    type = type.value,
    path = path,
    color = color,
    order = order,
    isNotified = isNotified,
    isExpanded = isExpanded,
    isSticky = isSticky
)

fun LabelEntity.toLabel() = Label(
    userId = userId,
    labelId = LabelId(labelId),
    parentId = parentId?.let { LabelId(it) },
    name = name,
    type = requireNotNull(LabelType.map[type]),
    path = path,
    color = color,
    order = order,
    isNotified = isNotified,
    isExpanded = isExpanded,
    isSticky = isSticky
)
