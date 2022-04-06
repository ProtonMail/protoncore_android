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

import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

val ProtonTypography.headline: TextStyle
    @Composable get() = headline.copy(color = ProtonTheme.colors.textNorm)

val ProtonTypography.headlineHint: TextStyle
    @Composable get() = headline.copy(color = ProtonTheme.colors.textHint)

val ProtonTypography.headlineSmall: TextStyle
    @Composable get() = body1Medium.copy(color = ProtonTheme.colors.textNorm)

val ProtonTypography.defaultHighlight: TextStyle
    @Composable get() = defaultHighlight()

@Composable
fun ProtonTypography.defaultHighlight(enabled: Boolean = true): TextStyle =
    body1Bold.copy(color = ProtonTheme.colors.textNorm(enabled))

val ProtonTypography.subheadline: TextStyle
    @Composable get() = subheadline.copy(color = ProtonTheme.colors.textNorm)

val ProtonTypography.defaultStrong: TextStyle
    @Composable get() = headlineSmall

@Composable
fun ProtonTypography.defaultStrong(enabled: Boolean): TextStyle =
    headlineSmall.copy(color = ProtonTheme.colors.textNorm(enabled))

val ProtonTypography.default: TextStyle
    @Composable get() = default()

@Composable
fun ProtonTypography.default(enabled: Boolean = true): TextStyle =
    body1Regular.copy(color = ProtonTheme.colors.textNorm(enabled))

val ProtonTypography.defaultWeak: TextStyle
    @Composable get() = defaultWeak()

@Composable
fun ProtonTypography.defaultWeak(enabled: Boolean = true): TextStyle =
    body1Regular.copy(color = ProtonTheme.colors.textWeak(enabled))

val ProtonTypography.defaultHint: TextStyle
    @Composable get() = body1Regular.copy(color = ProtonTheme.colors.textHint)

val ProtonTypography.defaultInverted: TextStyle
    @Composable get() = defaultInverted()

@Composable
fun ProtonTypography.defaultInverted(enabled: Boolean = true): TextStyle =
    body1Regular.copy(color = ProtonTheme.colors.textInverted(enabled))

val ProtonTypography.defaultSmallStrong: TextStyle
    @Composable get() = defaultSmallStrong()

@Composable
fun ProtonTypography.defaultSmallStrong(enabled: Boolean = true): TextStyle =
    body2Medium.copy(color = ProtonTheme.colors.textNorm(enabled))


val ProtonTypography.defaultSmallStrongInverted: TextStyle
    @Composable get() = defaultSmallStrongInverted()

@Composable
fun ProtonTypography.defaultSmallStrongInverted(enabled: Boolean = true): TextStyle =
    body2Medium.copy(color = ProtonTheme.colors.textInverted(enabled))

val ProtonTypography.defaultSmall: TextStyle
    @Composable get() = defaultSmall()

@Composable
fun ProtonTypography.defaultSmall(enabled: Boolean = true): TextStyle =
    body2Regular.copy(color = ProtonTheme.colors.textNorm(enabled))

val ProtonTypography.defaultSmallWeak: TextStyle
    @Composable get() = defaultSmallWeak()

@Composable
fun ProtonTypography.defaultSmallWeak(enabled: Boolean = true): TextStyle =
    body2Regular.copy(color = ProtonTheme.colors.textWeak(enabled))

val ProtonTypography.defaultSmallInverted: TextStyle
    @Composable get() = defaultSmallInverted()

@Composable
fun ProtonTypography.defaultSmallInverted(enabled: Boolean = true): TextStyle =
    body2Regular.copy(color = ProtonTheme.colors.textInverted(enabled))

val ProtonTypography.captionStrong: TextStyle
    @Composable get() = captionStrong()

@Composable
fun ProtonTypography.captionStrong(enabled: Boolean = true): TextStyle =
    captionMedium.copy(color = ProtonTheme.colors.textNorm(enabled))

val ProtonTypography.caption: TextStyle
    @Composable get() = caption()

@Composable
fun ProtonTypography.caption(enabled: Boolean = true): TextStyle =
    captionRegular.copy(color = ProtonTheme.colors.textNorm(enabled))

val ProtonTypography.captionWeak: TextStyle
    @Composable get() = captionWeak()

@Composable
fun ProtonTypography.captionWeak(enabled: Boolean = true): TextStyle =
    captionRegular.copy(color = ProtonTheme.colors.textWeak(enabled))

val ProtonTypography.captionHint: TextStyle
    @Composable get() = captionRegular.copy(color = ProtonTheme.colors.textHint)

val ProtonTypography.overline: TextStyle
    @Composable get() = overline()

@Composable
fun ProtonTypography.overline(enabled: Boolean = true): TextStyle =
    overlineRegular.copy(color = ProtonTheme.colors.textNorm(enabled))

val ProtonTypography.overlineStrong: TextStyle
    @Composable get() = overlineStrong()

@Composable
fun ProtonTypography.overlineStrong(enabled: Boolean = true): TextStyle =
    overlineMedium.copy(color = ProtonTheme.colors.textInverted(enabled))

@Immutable
data class ProtonTypography(
    internal val headline: TextStyle,
    internal val subheadline: TextStyle,
    internal val body1Regular: TextStyle,
    internal val body1Medium: TextStyle,
    internal val body1Bold: TextStyle,
    internal val body2Regular: TextStyle,
    internal val body2Medium: TextStyle,
    internal val captionRegular: TextStyle,
    internal val captionMedium: TextStyle,
    internal val overlineRegular: TextStyle,
    internal val overlineMedium: TextStyle,
) {

    constructor(
        defaultFontFamily: FontFamily = FontFamily.SansSerif,
        headline: TextStyle = TextStyle(
            fontSize = 20.sp,
            fontWeight = FontWeight.W700,
            letterSpacing = 0.01.em,
            lineHeight = 24.sp
        ),
        subheadline: TextStyle = TextStyle(
            fontSize = 20.sp,
            fontWeight = FontWeight.W400,
            letterSpacing = 0.01.em,
            lineHeight = 24.sp
        ),
        body1Regular: TextStyle = TextStyle(
            fontSize = 16.sp,
            fontWeight = FontWeight.W400,
            letterSpacing = 0.03.em,
            lineHeight = 24.sp
        ),
        body1Medium: TextStyle = body1Regular.copy(
            fontWeight = FontWeight.W500
        ),
        body1Bold: TextStyle = body1Regular.copy(
            fontWeight = FontWeight.W700
        ),
        body2Regular: TextStyle = TextStyle(
            fontSize = 14.sp,
            fontWeight = FontWeight.W400,
            letterSpacing = 0.02.em,
            lineHeight = 20.sp
        ),
        body2Medium: TextStyle = body2Regular.copy(
            fontWeight = FontWeight.W500
        ),
        captionRegular: TextStyle = TextStyle(
            fontSize = 14.sp,
            fontWeight = FontWeight.W400,
            letterSpacing = 0.03.em,
            lineHeight = 16.sp
        ),
        captionMedium: TextStyle = captionRegular.copy(
            fontWeight = FontWeight.W500
        ),
        overlineRegular: TextStyle = TextStyle(
            fontSize = 10.sp,
            fontWeight = FontWeight.W400,
            letterSpacing = 0.04.em,
            lineHeight = 16.sp
        ),
        overlineMedium: TextStyle = overlineRegular.copy(
            fontWeight = FontWeight.W500
        ),
    ) : this(
        headline = headline.withDefaultFontFamily(defaultFontFamily),
        subheadline = subheadline.withDefaultFontFamily(defaultFontFamily),
        body1Regular = body1Regular.withDefaultFontFamily(defaultFontFamily),
        body1Medium = body1Medium.withDefaultFontFamily(defaultFontFamily),
        body1Bold = body1Bold.withDefaultFontFamily(defaultFontFamily),
        body2Regular = body2Regular.withDefaultFontFamily(defaultFontFamily),
        body2Medium = body2Medium.withDefaultFontFamily(defaultFontFamily),
        captionRegular = captionRegular.withDefaultFontFamily(defaultFontFamily),
        captionMedium = captionMedium.withDefaultFontFamily(defaultFontFamily),
        overlineRegular = overlineRegular.withDefaultFontFamily(defaultFontFamily),
        overlineMedium = overlineMedium.withDefaultFontFamily(defaultFontFamily),
    )

    companion object {
        val Default = ProtonTypography()
    }
}

private fun TextStyle.withDefaultFontFamily(default: FontFamily): TextStyle =
    if (fontFamily != null) this else copy(fontFamily = default)

internal fun ProtonTypography.toMaterialThemeTypography() = Typography(
    defaultFontFamily = FontFamily.SansSerif,
    h6 = headline,
    subtitle1 = body1Medium,
    subtitle2 = body2Medium,
    body1 = body1Regular,
    body2 = body2Regular,
    button = body1Regular,
    caption = captionMedium,
    overline = overlineMedium
)

internal val LocalTypography = staticCompositionLocalOf { ProtonTypography() }
