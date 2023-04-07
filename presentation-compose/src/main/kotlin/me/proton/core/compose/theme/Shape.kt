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
package me.proton.core.compose.theme

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Immutable
data class ProtonShapes(
    val small: CornerBasedShape = RoundedCornerShape(ProtonDimens.DefaultCornerRadius),
    val medium: CornerBasedShape = RoundedCornerShape(ProtonDimens.LargeCornerRadius),
    val large: CornerBasedShape = RoundedCornerShape(ProtonDimens.ExtraLargeCornerRadius),
    val bottomSheet: Shape = RoundedCornerShape(
        topStart = ProtonDimens.LargeCornerRadius,
        topEnd = ProtonDimens.LargeCornerRadius,
        bottomStart = 0.dp,
        bottomEnd = 0.dp
    ),
)

fun ProtonShapes.toMaterialThemeShapes() = Shapes(
    small = small,
    medium = medium,
    large = large,
)

internal val LocalShapes = staticCompositionLocalOf { ProtonShapes() }
