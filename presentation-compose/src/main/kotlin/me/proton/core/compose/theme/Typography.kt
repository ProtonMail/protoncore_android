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

val ProtonTypography.headlineUnspecified: TextStyle
    @Composable get() = headline

val ProtonTypography.headlineNorm: TextStyle
    @Composable get() = headlineUnspecified.copy(color = ProtonTheme.colors.textNorm)

val ProtonTypography.headlineHint: TextStyle
    @Composable get() = headlineUnspecified.copy(color = ProtonTheme.colors.textHint)

@Deprecated(
    "Use headlineNorm or headlineUnspecified",
    replaceWith = ReplaceWith("headlineNorm")
)
val ProtonTypography.headline: TextStyle
    @Composable get() = headlineNorm

val ProtonTypography.headlineSmallUnspecified: TextStyle
    @Composable get() = body1Medium

val ProtonTypography.headlineSmallNorm: TextStyle
    @Composable get() = headlineSmallUnspecified.copy(color = ProtonTheme.colors.textNorm)

@Deprecated(
    "Use headlineSmallNorm or headlineSmallUnspecified",
    replaceWith = ReplaceWith("headlineSmallNorm")
)
val ProtonTypography.headlineSmall: TextStyle
    @Composable get() = headlineSmallNorm

val ProtonTypography.defaultHighlightUnspecified: TextStyle
    @Composable get() = body1Bold

val ProtonTypography.defaultHighlightNorm: TextStyle
    @Composable get() = defaultHighlightNorm()

@Composable
fun ProtonTypography.defaultHighlightNorm(enabled: Boolean = true): TextStyle =
    defaultHighlightUnspecified.copy(color = ProtonTheme.colors.textNorm(enabled))

@Deprecated(
    "Use defaultHighlightNorm or defaultHighlightUnspecified",
    replaceWith = ReplaceWith("defaultHighlightNorm")
)
val ProtonTypography.defaultHighlight: TextStyle
    @Composable get() = defaultHighlightNorm

@Deprecated(
    "Use defaultHighlightNorm or defaultHighlightUnspecified",
    replaceWith = ReplaceWith("defaultHighlightNorm")
)
@Composable
fun ProtonTypography.defaultHighlight(enabled: Boolean = true): TextStyle = defaultHighlightNorm(enabled)

val ProtonTypography.subheadlineUnspecified: TextStyle
    @Composable get() = subheadline

val ProtonTypography.subheadlineNorm: TextStyle
    @Composable get() = subheadlineUnspecified.copy(color = ProtonTheme.colors.textNorm)

@Deprecated(
    "Use subheadlineNorm or subheadlineUnspecified",
    replaceWith = ReplaceWith("subheadlineNorm")
)
val ProtonTypography.subheadline: TextStyle
    @Composable get() = subheadlineNorm

val ProtonTypography.defaultStrongUnspecified: TextStyle
    @Composable get() = body1Medium

val ProtonTypography.defaultStrongNorm: TextStyle
    @Composable get() = defaultStrongNorm()

@Composable
fun ProtonTypography.defaultStrongNorm(enabled: Boolean = true): TextStyle =
    defaultStrongUnspecified.copy(color = ProtonTheme.colors.textNorm(enabled))

@Deprecated(
    "Use defaultStrongNorm or defaultStrongUnspecified",
    replaceWith = ReplaceWith("defaultStrongNorm")
)
val ProtonTypography.defaultStrong: TextStyle
    @Composable get() = defaultStrongNorm

@Deprecated(
    "Use defaultStrongNorm or defaultStrongUnspecified",
    replaceWith = ReplaceWith("defaultStrongNorm")
)
@Composable
fun ProtonTypography.defaultStrong(enabled: Boolean): TextStyle = defaultStrongNorm(enabled)

val ProtonTypography.defaultUnspecified: TextStyle
    @Composable get() = body1Regular

val ProtonTypography.defaultNorm: TextStyle
    @Composable get() = defaultNorm()

@Composable
fun ProtonTypography.defaultNorm(enabled: Boolean = true): TextStyle =
    defaultUnspecified.copy(color = ProtonTheme.colors.textNorm(enabled))

val ProtonTypography.defaultWeak: TextStyle
    @Composable get() = defaultWeak()

@Composable
fun ProtonTypography.defaultWeak(enabled: Boolean = true): TextStyle =
    defaultUnspecified.copy(color = ProtonTheme.colors.textWeak(enabled))

val ProtonTypography.defaultHint: TextStyle
    @Composable get() = defaultUnspecified.copy(color = ProtonTheme.colors.textHint)

val ProtonTypography.defaultInverted: TextStyle
    @Composable get() = defaultInverted()

@Composable
fun ProtonTypography.defaultInverted(enabled: Boolean = true): TextStyle =
    defaultUnspecified.copy(color = ProtonTheme.colors.textInverted(enabled))

@Deprecated(
    "Use either defaultNorm or defaultUnspecified",
    replaceWith = ReplaceWith("defaultNorm")
)
val ProtonTypography.default: TextStyle
    @Composable get() = defaultNorm

@Deprecated(
    "Use either defaultNorm or defaultUnspecified",
    replaceWith = ReplaceWith("defaultNorm")
)
@Composable
fun ProtonTypography.default(enabled: Boolean = true): TextStyle = defaultNorm(enabled)

val ProtonTypography.defaultSmallStrongUnspecified: TextStyle
    @Composable get() = body2Medium

val ProtonTypography.defaultSmallStrongNorm: TextStyle
    @Composable get() = defaultSmallStrongNorm()

@Composable
fun ProtonTypography.defaultSmallStrongNorm(enabled: Boolean = true): TextStyle =
    defaultSmallStrongUnspecified.copy(color = ProtonTheme.colors.textNorm(enabled))

val ProtonTypography.defaultSmallStrongInverted: TextStyle
    @Composable get() = defaultSmallStrongInverted()

@Composable
fun ProtonTypography.defaultSmallStrongInverted(enabled: Boolean = true): TextStyle =
    defaultSmallStrongUnspecified.copy(color = ProtonTheme.colors.textInverted(enabled))

@Deprecated(
    "Use either defaultSmallStrongNorm or defaultSmallStrongUnspecified",
    replaceWith = ReplaceWith("defaultSmallStrongNorm)")
)
val ProtonTypography.defaultSmallStrong: TextStyle
    @Composable get() = defaultSmallStrongNorm

@Deprecated(
    "Use either defaultSmallStrongNorm or defaultSmallStrongUnspecified",
    replaceWith = ReplaceWith("defaultSmallStrongNorm)")
)
@Composable
fun ProtonTypography.defaultSmallStrong(enabled: Boolean = true): TextStyle = defaultSmallStrongNorm(enabled)

val ProtonTypography.defaultSmallUnspecified: TextStyle
    @Composable get() = body2Regular

val ProtonTypography.defaultSmallNorm: TextStyle
    @Composable get() = defaultSmallNorm()

@Composable
fun ProtonTypography.defaultSmallNorm(enabled: Boolean = true): TextStyle =
    defaultSmallUnspecified.copy(color = ProtonTheme.colors.textNorm(enabled))

val ProtonTypography.defaultSmallWeak: TextStyle
    @Composable get() = defaultSmallWeak()

@Composable
fun ProtonTypography.defaultSmallWeak(enabled: Boolean = true): TextStyle =
    defaultSmallUnspecified.copy(color = ProtonTheme.colors.textWeak(enabled))

val ProtonTypography.defaultSmallInverted: TextStyle
    @Composable get() = defaultSmallInverted()

@Composable
fun ProtonTypography.defaultSmallInverted(enabled: Boolean = true): TextStyle =
    defaultSmallUnspecified.copy(color = ProtonTheme.colors.textInverted(enabled))

@Deprecated(
    "Use defaultSmallNorm or defaultSmallUnspecified",
    replaceWith = ReplaceWith("defaultSmallNorm")
)
val ProtonTypography.defaultSmall: TextStyle
    @Composable get() = defaultSmallNorm

@Deprecated(
    "Use defaultSmallNorm or defaultSmallUnspecified",
    replaceWith = ReplaceWith("defaultSmallNorm")
)
@Composable
fun ProtonTypography.defaultSmall(enabled: Boolean = true): TextStyle = defaultSmallNorm(enabled)

val ProtonTypography.captionStrongUnspecified: TextStyle
    @Composable get() = captionMedium

val ProtonTypography.captionStrongNorm: TextStyle
    @Composable get() = captionStrongNorm()

@Composable
fun ProtonTypography.captionStrongNorm(enabled: Boolean = true): TextStyle =
    captionStrongUnspecified.copy(color = ProtonTheme.colors.textNorm(enabled))

@Deprecated(
    "Use captionStrongNorm or captionStrongUnspecified",
    replaceWith = ReplaceWith("captionStrongNorm")
)
val ProtonTypography.captionStrong: TextStyle
    @Composable get() = captionStrongNorm

@Deprecated(
    "Use captionStrongNorm or captionStrongUnspecified",
    replaceWith = ReplaceWith("captionStrongNorm")
)
@Composable
fun ProtonTypography.captionStrong(enabled: Boolean = true): TextStyle = captionStrongNorm(enabled)

val ProtonTypography.captionUnspecified: TextStyle
    @Composable get() = captionRegular

val ProtonTypography.captionNorm: TextStyle
    @Composable get() = captionNorm()

@Composable
fun ProtonTypography.captionNorm(enabled: Boolean = true): TextStyle =
    captionUnspecified.copy(color = ProtonTheme.colors.textNorm(enabled))

val ProtonTypography.captionWeak: TextStyle
    @Composable get() = captionWeak()

@Composable
fun ProtonTypography.captionWeak(enabled: Boolean = true): TextStyle =
    captionRegular.copy(color = ProtonTheme.colors.textWeak(enabled))

val ProtonTypography.captionHint: TextStyle
    @Composable get() = captionRegular.copy(color = ProtonTheme.colors.textHint)

@Deprecated(
    "Use captionNorm or captionUnspecified",
    replaceWith = ReplaceWith("captionNorm")
)
val ProtonTypography.caption: TextStyle
    @Composable get() = captionNorm()

@Deprecated(
    "Use captionNorm or captionUnspecified",
    replaceWith = ReplaceWith("captionNorm")
)
@Composable
fun ProtonTypography.caption(enabled: Boolean = true): TextStyle = captionNorm(enabled)

val ProtonTypography.overlineUnpsecified: TextStyle
    @Composable get() = overlineRegular

val ProtonTypography.overlineNorm: TextStyle
    @Composable get() = overlineNorm()

@Composable
fun ProtonTypography.overlineNorm(enabled: Boolean = true): TextStyle =
    overlineUnpsecified.copy(color = ProtonTheme.colors.textNorm(enabled))

val ProtonTypography.overlineWeak: TextStyle
    @Composable get() = overlineWeak()

@Composable
fun ProtonTypography.overlineWeak(enabled: Boolean = true): TextStyle =
    overlineUnpsecified.copy(color = ProtonTheme.colors.textWeak(enabled))

@Deprecated(
    "Use overlineNorm or overlineUnspecified",
    replaceWith = ReplaceWith("overlineNorm")
)
val ProtonTypography.overline: TextStyle
    @Composable get() = overlineNorm

@Deprecated(
    "Use overlineNorm or overlineUnspecified",
    replaceWith = ReplaceWith("overlineNorm")
)
@Composable
fun ProtonTypography.overline(enabled: Boolean = true): TextStyle = overlineNorm(enabled)

val ProtonTypography.overlineStrongUnspecified: TextStyle
    @Composable get() = overlineMedium

val ProtonTypography.overlineStrongNorm: TextStyle
    @Composable get() = overlineStrongNorm()

@Composable
fun ProtonTypography.overlineStrongNorm(enabled: Boolean = true): TextStyle =
    overlineStrongUnspecified.copy(color = ProtonTheme.colors.textInverted(enabled))

@Deprecated(
    "Use overlineStrongNorm or overlineStrongUnspecified",
    replaceWith = ReplaceWith("overlineStrongNorm")
)
val ProtonTypography.overlineStrong: TextStyle
    @Composable get() = overlineStrongNorm

@Deprecated(
    "Use overlineStrongNorm or overlineStrongUnspecified",
    replaceWith = ReplaceWith("overlineStrongNorm")
)
@Composable
fun ProtonTypography.overlineStrong(enabled: Boolean = true): TextStyle = overlineStrongNorm(enabled)

@Immutable
data class ProtonTypography(
    val headline: TextStyle,
    val subheadline: TextStyle,
    val body1Regular: TextStyle,
    val body1Medium: TextStyle,
    val body1Bold: TextStyle,
    val body2Regular: TextStyle,
    val body2Medium: TextStyle,
    val captionRegular: TextStyle,
    val captionMedium: TextStyle,
    val overlineRegular: TextStyle,
    val overlineMedium: TextStyle,
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
            fontSize = 12.sp,
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
            letterSpacing = 0.03.em,
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

internal fun ProtonTypography.toMaterial3ThemeTypography() = androidx.compose.material3.Typography(
    displayLarge = headline,
    displayMedium = headline,
    displaySmall = headline,
    headlineLarge = subheadline,
    headlineMedium = subheadline,
    headlineSmall = subheadline,
    titleLarge = body1Medium,
    titleMedium = body1Medium,
    titleSmall = body1Medium,
    bodyLarge = body1Regular,
    bodyMedium = body1Regular,
    bodySmall = body1Regular,
    labelLarge = captionMedium,
    labelMedium = captionMedium,
    labelSmall = captionMedium
)

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

val LocalTypography = staticCompositionLocalOf { ProtonTypography() }
