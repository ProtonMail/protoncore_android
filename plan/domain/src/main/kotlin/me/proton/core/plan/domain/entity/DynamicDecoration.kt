/*
 * Copyright (c) 2023 Proton AG
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

package me.proton.core.plan.domain.entity

import me.proton.core.domain.type.StringEnum

sealed class DynamicDecoration {
    data class Starred(val iconName: String) : DynamicDecoration()
    data class Badge(
        val text: String,
        val anchor: StringEnum<DynamicDecorationAnchor>?,
        val planId: String?
    ) : DynamicDecoration()
}

enum class DynamicDecorationAnchor(val anchor: String) {
    Title("title"),
    Subtitle("subtitle");

    companion object {
        val map = values().associateBy { it.anchor }
        fun enumOf(value: String?) = value?.let { StringEnum(it, map[it]) }
    }
}

private fun List<DynamicDecoration.Badge>.firstOrNull(
    anchor: DynamicDecorationAnchor,
    planId: String? = null
) = firstOrNull { it.anchor?.enum == anchor && it.planId == planId }

fun List<DynamicDecoration.Badge>.firstTitleOrNull(
    planId: String? = null
) = firstOrNull(DynamicDecorationAnchor.Title, planId)

fun List<DynamicDecoration.Badge>.firstSubtitleOrNull(
    planId: String? = null
) = firstOrNull(DynamicDecorationAnchor.Subtitle, planId)
