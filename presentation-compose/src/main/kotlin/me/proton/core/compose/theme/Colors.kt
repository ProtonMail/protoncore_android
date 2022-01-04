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

import androidx.compose.material.Colors
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.ui.graphics.Color

private object ProtonPalette {
    val Chambray = Color(0xFF3C4B88)
    val SanMarino = Color(0xFF5064B6)
    val CornflowerBlue = Color(0xFF657EE4)
    val Portage = Color(0xFF8498E9)
    val Perano = Color(0xFFA2B1EE)

    val Woodsmoke = Color(0xFF17181C)
    val Charade = Color(0xFF25272C)
    val Tuna = Color(0xFF303239)
    val Abbey = Color(0xFF494D55)
    val StormGray = Color(0xFF727680)
    val SantasGray = Color(0xFF9CA0AA)

    val PortGore = Color(0xFF1C223D)
    val PickledBluewood = Color(0xFF29304D)
    val Rhino = Color(0xFF353E60)

    val FrenchGray = Color(0xFFBABDC6)
    val Mischka = Color(0xFFDADCE3)
    val AthensGray = Color(0xFFEAECF1)
    val Whisper = Color(0xFFF5F6FA)

    val Pomegranate = Color(0xFFE84118)
    val Sunglow = Color(0xFFFBC531)
    val Apple = Color(0xFF44BD32)
}

@Stable
@Suppress("LongParameterList")
class ProtonColors(
    isDark: Boolean,

    shade100: Color,
    shade80: Color,
    shade60: Color,
    shade50: Color,
    shade40: Color,
    shade20: Color,
    shade10: Color,
    shade0: Color,

    textNorm: Color = shade100,
    textWeak: Color = shade80,
    textHint: Color = shade60,
    textDisabled: Color = shade50,
    textInverted: Color = shade0,

    iconNorm: Color = shade100,
    iconWeak: Color = shade80,
    iconHint: Color = shade60,
    iconDisabled: Color = shade50,
    iconInverted: Color = shade0,

    interactionStrongNorm: Color = shade100,
    interactionStrongPressed: Color = shade80,

    interactionWeakNorm: Color = shade20,
    interactionWeakPressed: Color = shade40,
    interactionWeakDisabled: Color = shade10,

    backgroundNorm: Color = shade0,
    backgroundSecondary: Color = shade10,

    separatorNorm: Color = shade20,

    blenderNorm: Color,

    brandDarken40: Color = ProtonPalette.Chambray,
    brandDarken20: Color = ProtonPalette.SanMarino,
    brandNorm: Color = ProtonPalette.CornflowerBlue,
    brandLighten20: Color = ProtonPalette.Portage,
    brandLighten40: Color = ProtonPalette.Perano,

    notificationNorm: Color = shade100,
    notificationError: Color = ProtonPalette.Pomegranate,
    notificationWarning: Color = ProtonPalette.Sunglow,
    notificationSuccess: Color = ProtonPalette.Apple,

    interactionNorm: Color = brandNorm,
    interactionPressed: Color = brandDarken20,
    interactionDisabled: Color = brandLighten40,

    floatyBackground: Color = ProtonPalette.Tuna,
    floatyPressed: Color = ProtonPalette.Woodsmoke,
    floatyText: Color = Color.White,

    shadowNorm: Color,
    shadowRaised: Color,
    shadowLifted: Color,

    sidebarColors: ProtonColors? = null,
) {
    var isDark: Boolean by mutableStateOf(isDark, structuralEqualityPolicy())
        internal set

    var shade100: Color by mutableStateOf(shade100, structuralEqualityPolicy())
        internal set
    var shade80: Color by mutableStateOf(shade80, structuralEqualityPolicy())
        internal set
    var shade60: Color by mutableStateOf(shade60, structuralEqualityPolicy())
        internal set
    var shade50: Color by mutableStateOf(shade50, structuralEqualityPolicy())
        internal set
    var shade40: Color by mutableStateOf(shade40, structuralEqualityPolicy())
        internal set
    var shade20: Color by mutableStateOf(shade20, structuralEqualityPolicy())
        internal set
    var shade10: Color by mutableStateOf(shade10, structuralEqualityPolicy())
        internal set
    var shade0: Color by mutableStateOf(shade0, structuralEqualityPolicy())
        internal set

    var textNorm: Color by mutableStateOf(textNorm, structuralEqualityPolicy())
        internal set
    var textWeak: Color by mutableStateOf(textWeak, structuralEqualityPolicy())
        internal set
    var textHint: Color by mutableStateOf(textHint, structuralEqualityPolicy())
        internal set
    var textDisabled: Color by mutableStateOf(textDisabled, structuralEqualityPolicy())
        internal set
    var textInverted: Color by mutableStateOf(textInverted, structuralEqualityPolicy())
        internal set

    var iconNorm: Color by mutableStateOf(iconNorm, structuralEqualityPolicy())
        internal set
    var iconWeak: Color by mutableStateOf(iconWeak, structuralEqualityPolicy())
        internal set
    var iconHint: Color by mutableStateOf(iconHint, structuralEqualityPolicy())
        internal set
    var iconDisabled: Color by mutableStateOf(iconDisabled, structuralEqualityPolicy())
        internal set
    var iconInverted: Color by mutableStateOf(iconInverted, structuralEqualityPolicy())
        internal set

    var interactionStrongNorm: Color by mutableStateOf(interactionStrongNorm, structuralEqualityPolicy())
        internal set
    var interactionStrongPressed: Color by mutableStateOf(interactionStrongPressed, structuralEqualityPolicy())
        internal set

    var interactionWeakNorm: Color by mutableStateOf(interactionWeakNorm, structuralEqualityPolicy())
        internal set
    var interactionWeakPressed: Color by mutableStateOf(interactionWeakPressed, structuralEqualityPolicy())
        internal set
    var interactionWeakDisabled: Color by mutableStateOf(interactionWeakDisabled, structuralEqualityPolicy())
        internal set

    var backgroundNorm: Color by mutableStateOf(backgroundNorm, structuralEqualityPolicy())
        internal set
    var backgroundSecondary: Color by mutableStateOf(backgroundSecondary, structuralEqualityPolicy())
        internal set

    var separatorNorm: Color by mutableStateOf(separatorNorm, structuralEqualityPolicy())
        internal set

    var blenderNorm: Color by mutableStateOf(blenderNorm, structuralEqualityPolicy())
        internal set

    var brandDarken40: Color by mutableStateOf(brandDarken40, structuralEqualityPolicy())
        internal set
    var brandDarken20: Color by mutableStateOf(brandDarken20, structuralEqualityPolicy())
        internal set
    var brandNorm: Color by mutableStateOf(brandNorm, structuralEqualityPolicy())
        internal set
    var brandLighten20: Color by mutableStateOf(brandLighten20, structuralEqualityPolicy())
        internal set
    var brandLighten40: Color by mutableStateOf(brandLighten40, structuralEqualityPolicy())
        internal set

    var notificationNorm: Color by mutableStateOf(notificationNorm, structuralEqualityPolicy())
        internal set
    var notificationError: Color by mutableStateOf(notificationError, structuralEqualityPolicy())
        internal set
    var notificationWarning: Color by mutableStateOf(notificationWarning, structuralEqualityPolicy())
        internal set
    var notificationSuccess: Color by mutableStateOf(notificationSuccess, structuralEqualityPolicy())
        internal set

    var interactionNorm: Color by mutableStateOf(interactionNorm, structuralEqualityPolicy())
        internal set
    var interactionPressed: Color by mutableStateOf(interactionPressed, structuralEqualityPolicy())
        internal set
    var interactionDisabled: Color by mutableStateOf(interactionDisabled, structuralEqualityPolicy())
        internal set

    var floatyBackground: Color by mutableStateOf(floatyBackground, structuralEqualityPolicy())
        internal set
    var floatyPressed: Color by mutableStateOf(floatyPressed, structuralEqualityPolicy())
        internal set
    var floatyText: Color by mutableStateOf(floatyText, structuralEqualityPolicy())
        internal set

    var shadowNorm: Color by mutableStateOf(shadowNorm, structuralEqualityPolicy())
        internal set
    var shadowRaised: Color by mutableStateOf(shadowRaised, structuralEqualityPolicy())
        internal set
    var shadowLifted: Color by mutableStateOf(shadowLifted, structuralEqualityPolicy())
        internal set

    var sidebarColors: ProtonColors? by mutableStateOf(sidebarColors, structuralEqualityPolicy())

    fun copy(
        isDark: Boolean = this.isDark,

        shade100: Color = this.shade100,
        shade80: Color = this.shade80,
        shade60: Color = this.shade60,
        shade50: Color = this.shade50,
        shade40: Color = this.shade40,
        shade20: Color = this.shade20,
        shade10: Color = this.shade10,
        shade0: Color = this.shade0,

        textNorm: Color = this.textNorm,
        textWeak: Color = this.textWeak,
        textHint: Color = this.textHint,
        textDisabled: Color = this.textDisabled,
        textInverted: Color = this.textInverted,

        iconNorm: Color = this.iconNorm,
        iconWeak: Color = this.iconWeak,
        iconHint: Color = this.iconHint,
        iconDisabled: Color = this.iconDisabled,
        iconInverted: Color = this.iconInverted,

        interactionStrongNorm: Color = this.interactionStrongNorm,
        interactionStrongPressed: Color = this.interactionStrongPressed,

        interactionWeakNorm: Color = this.interactionWeakNorm,
        interactionWeakPressed: Color = this.interactionWeakPressed,
        interactionWeakDisabled: Color = this.interactionWeakDisabled,

        backgroundNorm: Color = this.backgroundNorm,
        backgroundSecondary: Color = this.backgroundSecondary,

        separatorNorm: Color = this.separatorNorm,

        blenderNorm: Color = this.blenderNorm,

        brandDarken40: Color = this.brandDarken40,
        brandDarken20: Color = this.brandDarken20,
        brandNorm: Color = this.brandNorm,
        brandLighten20: Color = this.brandLighten20,
        brandLighten40: Color = this.brandLighten40,

        notificationNorm: Color = this.notificationNorm,
        notificationError: Color = this.notificationError,
        notificationWarning: Color = this.notificationWarning,
        notificationSuccess: Color = this.notificationSuccess,

        interactionNorm: Color = this.interactionNorm,
        interactionPressed: Color = this.interactionPressed,
        interactionDisabled: Color = this.interactionDisabled,

        floatyBackground: Color = this.floatyBackground,
        floatyPressed: Color = this.floatyPressed,
        floatyText: Color = this.floatyText,

        shadowNorm: Color = this.shadowNorm,
        shadowRaised: Color = this.shadowRaised,
        shadowLifted: Color = this.shadowLifted,

        sidebarColors: ProtonColors? = this.sidebarColors,
    ) = ProtonColors(
        isDark = isDark,

        shade100 = shade100,
        shade80 = shade80,
        shade60 = shade60,
        shade50 = shade50,
        shade40 = shade40,
        shade20 = shade20,
        shade10 = shade10,
        shade0 = shade0,

        textNorm = textNorm,
        textWeak = textWeak,
        textHint = textHint,
        textDisabled = textDisabled,
        textInverted = textInverted,

        iconNorm = iconNorm,
        iconWeak = iconWeak,
        iconHint = iconHint,
        iconDisabled = iconDisabled,
        iconInverted = iconInverted,

        interactionStrongNorm = interactionStrongNorm,
        interactionStrongPressed = interactionStrongPressed,

        interactionWeakNorm = interactionWeakNorm,
        interactionWeakPressed = interactionWeakPressed,
        interactionWeakDisabled = interactionWeakDisabled,

        backgroundNorm = backgroundNorm,
        backgroundSecondary = backgroundSecondary,

        separatorNorm = separatorNorm,

        blenderNorm = blenderNorm,

        brandDarken40 = brandDarken40,
        brandDarken20 = brandDarken20,
        brandNorm = brandNorm,
        brandLighten20 = brandLighten20,
        brandLighten40 = brandLighten40,

        notificationNorm = notificationNorm,
        notificationError = notificationError,
        notificationWarning = notificationWarning,
        notificationSuccess = notificationSuccess,

        interactionNorm = interactionNorm,
        interactionPressed = interactionPressed,
        interactionDisabled = interactionDisabled,

        floatyBackground = floatyBackground,
        floatyPressed = floatyPressed,
        floatyText = floatyText,

        shadowNorm = shadowNorm,
        shadowRaised = shadowRaised,
        shadowLifted = shadowLifted,

        sidebarColors = sidebarColors,
    )

    companion object {

        private val BaseLight = ProtonColors(
            isDark = false,
            shade100 = ProtonPalette.Woodsmoke,
            shade80 = ProtonPalette.StormGray,
            shade60 = ProtonPalette.SantasGray,
            shade50 = ProtonPalette.FrenchGray,
            shade40 = ProtonPalette.Mischka,
            shade20 = ProtonPalette.AthensGray,
            shade10 = ProtonPalette.Whisper,
            shade0 = Color.White,
            shadowNorm = Color.Black.copy(alpha = 0.1f),
            shadowRaised = Color.Black.copy(alpha = 0.1f),
            shadowLifted = Color.Black.copy(alpha = 0.1f),
            blenderNorm = ProtonPalette.Woodsmoke.copy(alpha = 0.48f),
        )

        private val BaseDark = ProtonColors(
            isDark = true,
            shade100 = Color.White,
            shade80 = ProtonPalette.SantasGray,
            shade60 = ProtonPalette.StormGray,
            shade50 = ProtonPalette.Abbey,
            shade40 = ProtonPalette.Abbey,
            shade20 = ProtonPalette.Tuna,
            shade10 = ProtonPalette.Charade,
            shade0 = ProtonPalette.Woodsmoke,
            shadowNorm = Color.Black.copy(alpha = 0.8f),
            shadowRaised = Color.Black.copy(alpha = 0.8f),
            shadowLifted = Color.Black.copy(alpha = 0.86f),
            blenderNorm = Color.Black.copy(alpha = 0.52f),
        )

        private val SidebarLight = BaseLight.copy(
            backgroundNorm = ProtonPalette.PortGore,
            backgroundSecondary = ProtonPalette.PortGore,
            interactionWeakNorm = ProtonPalette.PickledBluewood,
            interactionWeakPressed = ProtonPalette.Rhino,
            separatorNorm = ProtonPalette.PickledBluewood,
            textNorm = Color.White,
            textWeak = ProtonPalette.SantasGray,
            iconNorm = Color.White,
            iconWeak = ProtonPalette.SantasGray,
            interactionPressed = ProtonPalette.SanMarino,
        )

        private val SidebarDark = BaseDark

        val Light = BaseLight.copy(sidebarColors = SidebarLight)
        val Dark = BaseDark.copy(sidebarColors = SidebarDark)
    }
}

fun ProtonColors.textNorm(enabled: Boolean = true) = if (enabled) textNorm else textDisabled
fun ProtonColors.textWeak(enabled: Boolean = true) = if (enabled) textWeak else textDisabled
fun ProtonColors.textInverted(enabled: Boolean = true) = if (enabled) textInverted else textDisabled

internal fun ProtonColors.toMaterialThemeColors() = Colors(
    primary = brandNorm,
    primaryVariant = brandDarken20,
    secondary = brandNorm,
    secondaryVariant = brandDarken20,
    background = backgroundNorm,
    surface = backgroundSecondary,
    error = notificationError,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = textNorm,
    onSurface = textNorm,
    onError = textInverted,
    isLight = !isDark,
)

internal fun ProtonColors.updateColorsFrom(other: ProtonColors) {
    isDark = other.isDark

    shade100 = other.shade100
    shade80 = other.shade80
    shade60 = other.shade60
    shade50 = other.shade50
    shade40 = other.shade40
    shade20 = other.shade20
    shade10 = other.shade10
    shade0 = other.shade0

    textNorm = other.textNorm
    textWeak = other.textWeak
    textHint = other.textHint
    textDisabled = other.textDisabled
    textInverted = other.textInverted

    iconNorm = other.iconNorm
    iconWeak = other.iconWeak
    iconHint = other.iconHint
    iconDisabled = other.iconDisabled
    iconInverted = other.iconInverted

    interactionStrongNorm = other.interactionStrongNorm
    interactionStrongPressed = other.interactionStrongPressed

    interactionWeakNorm = other.interactionWeakNorm
    interactionWeakPressed = other.interactionWeakPressed
    interactionWeakDisabled = other.interactionWeakDisabled

    backgroundNorm = other.backgroundNorm
    backgroundSecondary = other.backgroundSecondary

    separatorNorm = other.separatorNorm

    blenderNorm = other.blenderNorm

    brandDarken40 = other.brandDarken40
    brandDarken20 = other.brandDarken20
    brandNorm = other.brandNorm
    brandLighten20 = other.brandLighten20
    brandLighten40 = other.brandLighten40

    notificationNorm = other.notificationNorm
    notificationError = other.notificationError
    notificationWarning = other.notificationWarning
    notificationSuccess = other.notificationSuccess

    interactionNorm = other.interactionNorm
    interactionPressed = other.interactionPressed
    interactionDisabled = other.interactionDisabled

    floatyBackground = other.floatyBackground
    floatyPressed = other.floatyPressed
    floatyText = other.floatyText

    shadowNorm = other.shadowNorm
    shadowRaised = other.shadowRaised
    shadowLifted = other.shadowLifted

    sidebarColors = other.sidebarColors
}

internal val LocalColors = staticCompositionLocalOf { ProtonColors.Light }
