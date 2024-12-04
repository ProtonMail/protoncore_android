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

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember

/**
 * App Theme to provide for wrapping Compose screen.
 *
 * [AppTheme] should use [ProtonTheme] or [ProtonTheme3] for compatibility and consistency reasons.
 *
 * Example of usage:
 * ```kotlin
 * @Provides
 * fun provideAppTheme() = AppTheme { content ->
 *     MyAppTheme { content() }
 * }
 * ...
 * @Composable
 * fun MyAppTheme(
 *     isDark: Boolean = isNightMode(),
 *     colors: ProtonColors = if (isDark) ProtonColors.Dark else ProtonColors.Light,
 *     typography: ProtonTypography = ProtonTypography.Default,
 *     shapes: ProtonShapes = ProtonShapes(),
 *     content: @Composable () -> Unit
 * ) {
 *     ProtonTheme(
 *         isDark = isDark,
 *         colors = colors.copy(
 *             brandNorm = Color(0xFF5252CC),
 *             interactionNorm = Color(0xFF8A6EFF),
 *         ),
 *         typography = ProtonTypography.Default.copy(
 *             hero = typography.hero.copy(fontSize = 30.sp),
 *         ),
 *         shapes = shapes.copy(
 *             small = RoundedCornerShape(Radius.small + Radius.small),
 *             medium = RoundedCornerShape(Radius.medium + Radius.small),
 *             large = RoundedCornerShape(Radius.large + Radius.small),
 *         ),
 *         content = content
 *     )
 * }
 * ...
 * @AndroidEntryPoint
 * class MyActivity : ProtonActivity() {
 *     ...
 *     @Inject lateinit var appTheme: AppTheme
 *     ...
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *         ...
 *         setContent {
 *             appTheme {
 *                 ...
 *             }
 *         }
 *     }
 * }
 * ````
 */
fun interface AppTheme {
    @Composable
    operator fun invoke(content: @Composable () -> Unit)
}

@Composable
fun ProtonTheme3(
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
    ) {
        androidx.compose.material3.MaterialTheme(
            typography = typography.toMaterial3ThemeTypography(),
            colorScheme = rememberedColors.toMaterial3ThemeColors(),
            content = content
        )
    }
}

@Composable
fun ProtonTheme(
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
    ) {
        MaterialTheme(
            colors = rememberedColors.toMaterialThemeColors(),
            typography = typography.toMaterialThemeTypography(),
            shapes = shapes.toMaterialThemeShapes(),
            content = content
        )
    }
}

@Composable
fun isNightMode() = when (AppCompatDelegate.getDefaultNightMode()) {
    AppCompatDelegate.MODE_NIGHT_NO -> false
    AppCompatDelegate.MODE_NIGHT_YES -> true
    else -> isSystemInDarkTheme()
}

object ProtonTheme {
    val colors: ProtonColors
        @Composable
        @ReadOnlyComposable
        get() = LocalColors.current

    val typography: ProtonTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalTypography.current

    val shapes: ProtonShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalShapes.current
}
