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

package me.proton.core.label.domain.entity

import me.proton.core.domain.entity.UserId

data class Label(
    val userId: UserId,
    val labelId: LabelId,
    /**
     * Encrypted label id of parent folder (default: root level).
     */
    val parentId: LabelId?,
    /**
     * Name, cannot be same as an existing label of this Type. Max length is 100 characters.
     */
    val name: String,
    /**
     * Type of Label (e.g. MessageLabel, MessageFolder, ContactGroup).
     */
    val type: LabelType,
    /**
     * Path according [parentId] (e.g. "ParentName/LabelName").
     */
    val path: String,
    /**
     * Color in RGB format (e.g. "#RRGGBB") - must match default colors.
     */
    val color: String,
    /**
     * Order of Labels (priority).
     */
    val order: Int,
    /**
     * Returns true if notification are enabled (default), false otherwise.
     */
    val isNotified: Boolean?,
    /**
     * Returns true if expanded and show sub-folders, false otherwise.
     */
    val isExpanded: Boolean?,
    /**
     * Returns true if stick to the page in sidebar, false otherwise.
     */
    val isSticky: Boolean?,
)

/**
 * Representing a Label before creation.
 */
data class NewLabel(
    val parentId: LabelId?,
    val name: String,
    val type: LabelType,
    val color: String,
    val isNotified: Boolean?,
    val isExpanded: Boolean?,
    val isSticky: Boolean?,
)

/**
 * Representing a Label to update.
 */
data class UpdateLabel(
    val labelId: LabelId,
    val parentId: LabelId?,
    val name: String,
    val color: String,
    val isNotified: Boolean?,
    val isExpanded: Boolean?,
    val isSticky: Boolean?,
)

fun Label.toUpdateLabel() = UpdateLabel(
    labelId = labelId,
    parentId = parentId,
    name = name,
    color = color,
    isNotified = isNotified,
    isExpanded = isExpanded,
    isSticky = isSticky,
)
