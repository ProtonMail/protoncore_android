/*
 * Copyright (c) 2024 Proton AG
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

package me.proton.core.presentation.compose.tv.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.tv.material3.ColorScheme
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.LocalTextStyle
import androidx.tv.material3.MaterialTheme
import me.proton.core.compose.theme.LocalColors
import me.proton.core.compose.theme.LocalShapes
import me.proton.core.compose.theme.LocalTypography
import me.proton.core.compose.theme.ProtonColors
import me.proton.core.compose.theme.ProtonShapes
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.ProtonTypography
import me.proton.core.compose.theme.defaultUnspecified
import me.proton.core.compose.theme.isNightMode
import me.proton.core.compose.theme.updateColorsFrom

@Composable
fun ProtonThemeTv(
    isDark: Boolean = isNightMode(),
    colors: ProtonColors = if (isDark) ProtonColors.Dark else ProtonColors.Light,
    typography: ProtonTypography = ProtonTypography.Default,
    shapes: ProtonShapes = ProtonShapes(),
    content: @Composable () -> Unit
) {
    val rememberedColors = remember { colors.copy() }.apply { updateColorsFrom(colors) }

    CompositionLocalProvider(
        LocalColors provides rememberedColors,
        LocalTypography provides typography,
        LocalShapes provides shapes,
        LocalContentColor provides rememberedColors.textNorm,
        LocalTextStyle provides ProtonTheme.typography.defaultUnspecified
    ) {
        MaterialTheme(
            colorScheme = colors.toMaterialTvColors(),
            content = content
        )
    }
}

private fun ProtonColors.toMaterialTvColors() = ColorScheme(
    // Try to assign the main colors for some sane defaults. In practice we use ProtonColors directly in most cases.
    primary = brandNorm,
    onPrimary = Color.White,
    background = backgroundNorm,
    onBackground = textNorm,
    surface = backgroundSecondary,
    onSurface = textNorm,
    surfaceVariant = backgroundNorm,
    onSurfaceVariant = textWeak,
    inverseSurface = shade100,
    inverseOnSurface = textInverted,
    surfaceTint = Color.Transparent,
    error = notificationError,
    onError = textInverted,
    scrim = blenderNorm,
    border = separatorNorm,

    // The rest doesn't match our scheme very well.
    primaryContainer = brandNorm,
    onPrimaryContainer = Color.White,
    inversePrimary = Color.White,
    secondary = brandNorm,
    onSecondary = Color.White,
    secondaryContainer = backgroundSecondary,
    onSecondaryContainer = textNorm,
    tertiary = brandDarken20,
    onTertiary = Color.White,
    tertiaryContainer = backgroundNorm,
    onTertiaryContainer = textNorm,
    errorContainer = backgroundNorm,
    onErrorContainer = textNorm,
    borderVariant = separatorNorm,
)
