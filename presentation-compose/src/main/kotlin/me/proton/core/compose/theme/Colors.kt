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
import androidx.compose.ui.graphics.toArgb
import me.proton.core.presentation.utils.ProtonColorUtils.intenseColorVariant
import me.proton.core.presentation.utils.ProtonColorUtils.strongColorVariant

private object ProtonPalette {
    val Haiti = Color(0xFF1B1340)
    val Valhalla = Color(0xFF271B54)
    val Jacarta = Color(0xFF2E2260)
    val Chambray = Color(0xFF372580)
    val SanMarino = Color(0xFF4D34B3)
    val CornflowerBlue = Color(0xFF6D4AFF)
    val Portage = Color(0xFF8A6EFF)
    val Perano = Color(0xFFC4B7FF)

    val BalticSea = Color(0xFF1C1B24)
    val Bastille = Color(0xFF292733)
    val SteelGray = Color(0xFF343140)
    val BlackCurrant = Color(0xFF3B3747)
    val GunPowder = Color(0xFF4A4658)
    val Smoky = Color(0xFF5B576B)
    val Dolphin = Color(0xFF6D697D)
    val CadetBlue = Color(0xFFA7A4B5)
    val Cinder = Color(0xFF0C0C14)
    val ShipGray = Color(0xFF35333D)
    val DoveGray = Color(0xFF706D6B)
    val Dawn = Color(0xFF999693)
    val CottonSeed = Color(0xFFC2BFBC)
    val Cloud = Color(0xFFD1CFCD)
    val Ebb = Color(0xFFEAE7E4)
    val Pampas = Color(0xFFF1EEEB)
    val Carrara = Color(0xFFF5F4F2)
    val White = Color(0xFFFFFFFF)

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

    val Pomegranate = Color(0xFFCC2D4F)
    val Mauvelous = Color(0xFFF08FA4)
    val Sunglow = Color(0xFFE65200)
    val TexasRose = Color(0xFFFFB84D)
    val Apple = Color(0xFF007B58)
    val PuertoRico = Color(0xFF4AB89A)

    // New accent colors for rebranding
    val PurpleBase = Color(0xFF8080FF)
    val EnzianBase = Color(0xFF5252CC)
    val PinkBase = Color(0xFFDB60D6)
    val PlumBase = Color(0xFFA839A4)
    val StrawberryBase = Color(0xFFEC3E7C)
    val CeriseBase = Color(0xFFBA1E55)
    val CarrotBase = Color(0xFFF78400)
    val CopperBase = Color(0xFFC44800)
    val SaharaBase = Color(0xFF936D58)
    val SoilBase = Color(0xFF54473F)
    val SlateBlueBase = Color(0xFF415DF0)
    val CobaltBase = Color(0xFF273EB2)
    val PacificBase = Color(0xFF179FD9)
    val OceanBase = Color(0xFF0A77A6)
    val ReefBase = Color(0xFF1DA583)
    val PineBase = Color(0xFF0F735A)
    val FernBase = Color(0xFF3CBB3A)
    val ForestBase = Color(0xFF258723)
    val OliveBase = Color(0xFFB4A40E)
    val PickleBase = Color(0xFF807304)
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
    shade15: Color,
    shade10: Color,
    shade0: Color,

    brandDarken40: Color = ProtonPalette.Chambray,
    brandDarken20: Color = ProtonPalette.SanMarino,
    brandNorm: Color = ProtonPalette.CornflowerBlue,
    brandLighten20: Color = ProtonPalette.Portage,
    brandLighten40: Color = ProtonPalette.Perano,

    textNorm: Color = shade100,
    textAccent: Color = brandNorm,
    textWeak: Color = shade80,
    textHint: Color = shade60,
    textDisabled: Color = shade50,
    textInverted: Color = shade0,

    iconNorm: Color = shade100,
    iconAccent: Color = brandNorm,
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
    backgroundDeep: Color = shade15,

    separatorNorm: Color = shade20,

    blenderNorm: Color,

    notificationNorm: Color = shade100,
    notificationError: Color = ProtonPalette.Pomegranate,
    notificationWarning: Color = ProtonPalette.Sunglow,
    notificationSuccess: Color = ProtonPalette.Apple,

    interactionNorm: Color = brandNorm,
    interactionPressed: Color = brandDarken20,
    interactionDisabled: Color = brandLighten40,

    floatyBackground: Color = ProtonPalette.ShipGray,
    floatyPressed: Color = ProtonPalette.Cinder,
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
    var shade15: Color by mutableStateOf(shade15, structuralEqualityPolicy())
        internal set
    var shade10: Color by mutableStateOf(shade10, structuralEqualityPolicy())
        internal set
    var shade0: Color by mutableStateOf(shade0, structuralEqualityPolicy())
        internal set

    var textNorm: Color by mutableStateOf(textNorm, structuralEqualityPolicy())
        internal set
    var textAccent: Color by mutableStateOf(textAccent, structuralEqualityPolicy())
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
    var iconAccent: Color by mutableStateOf(iconAccent, structuralEqualityPolicy())
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
    var backgroundDeep: Color by mutableStateOf(backgroundDeep, structuralEqualityPolicy())
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
        shade15: Color = this.shade15,
        shade10: Color = this.shade10,
        shade0: Color = this.shade0,

        textNorm: Color = this.textNorm,
        textAccent: Color = this.textAccent,
        textWeak: Color = this.textWeak,
        textHint: Color = this.textHint,
        textDisabled: Color = this.textDisabled,
        textInverted: Color = this.textInverted,

        iconNorm: Color = this.iconNorm,
        iconAccent: Color = this.iconAccent,
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
        backgroundDeep: Color = this.backgroundDeep,

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
        shade15 = shade15,
        shade10 = shade10,
        shade0 = shade0,

        textNorm = textNorm,
        textAccent = textAccent,
        textWeak = textWeak,
        textHint = textHint,
        textDisabled = textDisabled,
        textInverted = textInverted,

        iconNorm = iconNorm,
        iconAccent = iconAccent,
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
        backgroundDeep = backgroundDeep,

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

        val Light = baseLight().copy(sidebarColors = sidebarLight())
        val Dark = baseDark().copy(sidebarColors = sidebarDark())

        private fun baseLight(
            brandDarken40: Color = ProtonPalette.Chambray,
            brandDarken20: Color = ProtonPalette.SanMarino,
            brandNorm: Color = ProtonPalette.CornflowerBlue,
            brandLighten20: Color = ProtonPalette.Portage,
            brandLighten40: Color = ProtonPalette.Perano,
        ) = ProtonColors(
            isDark = false,
            brandDarken40 = brandDarken40,
            brandDarken20 = brandDarken20,
            brandNorm = brandNorm,
            brandLighten20 = brandLighten20,
            brandLighten40 = brandLighten40,
            notificationError = ProtonPalette.Pomegranate,
            notificationWarning = ProtonPalette.Sunglow,
            notificationSuccess = ProtonPalette.Apple,
            shade100 = ProtonPalette.Cinder,
            shade80 = ProtonPalette.DoveGray,
            shade60 = ProtonPalette.Dawn,
            shade50 = ProtonPalette.CottonSeed,
            shade40 = ProtonPalette.Cloud,
            shade20 = ProtonPalette.Ebb,
            shade15 = ProtonPalette.Pampas,
            shade10 = ProtonPalette.Carrara,
            shade0 = Color.White,
            shadowNorm = Color.Black.copy(alpha = 0.1f),
            shadowRaised = Color.Black.copy(alpha = 0.1f),
            shadowLifted = Color.Black.copy(alpha = 0.1f),
            blenderNorm = ProtonPalette.Woodsmoke.copy(alpha = 0.48f),
            textAccent = brandNorm,
            iconAccent = brandNorm,
        )

        private fun baseDark(
            brandDarken40: Color = ProtonPalette.Chambray,
            brandDarken20: Color = ProtonPalette.SanMarino,
            brandNorm: Color = ProtonPalette.CornflowerBlue,
            brandLighten20: Color = ProtonPalette.Portage,
            brandLighten40: Color = ProtonPalette.Perano,
        ) = ProtonColors(
            isDark = true,
            brandDarken40 = brandDarken40,
            brandDarken20 = brandDarken20,
            brandNorm = brandNorm,
            brandLighten20 = brandLighten20,
            brandLighten40 = brandLighten40,
            notificationError = ProtonPalette.Mauvelous,
            notificationWarning = ProtonPalette.TexasRose,
            notificationSuccess = ProtonPalette.PuertoRico,
            shade100 = Color.White,
            shade80 = ProtonPalette.CadetBlue,
            shade60 = ProtonPalette.Dolphin,
            shade50 = ProtonPalette.Smoky,
            shade40 = ProtonPalette.GunPowder,
            shade20 = ProtonPalette.BlackCurrant,
            shade15 = ProtonPalette.Bastille,
            shade10 = ProtonPalette.BalticSea,
            shade0 = ProtonPalette.Cinder,
            shadowNorm = Color.Black.copy(alpha = 0.8f),
            shadowRaised = Color.Black.copy(alpha = 0.8f),
            shadowLifted = Color.Black.copy(alpha = 0.86f),
            blenderNorm = Color.Black.copy(alpha = 0.52f),
            textAccent = brandLighten20,
            iconAccent = brandLighten20,
        ).let {
            it.copy(
                interactionWeakNorm = it.shade20,
                interactionWeakPressed = it.shade40,
                interactionWeakDisabled = it.shade15,
                backgroundNorm = it.shade10,
                backgroundSecondary = it.shade15,
                backgroundDeep = it.shade0,
            )
        }

        private fun sidebarLight(
            brandDarken40: Color = ProtonPalette.Chambray,
            brandDarken20: Color = ProtonPalette.SanMarino,
            brandNorm: Color = ProtonPalette.CornflowerBlue,
            brandLighten20: Color = ProtonPalette.Portage,
            brandLighten40: Color = ProtonPalette.Perano,
        ) = baseLight(
            brandDarken40 = brandDarken40,
            brandDarken20 = brandDarken20,
            brandNorm = brandNorm,
            brandLighten20 = brandLighten20,
            brandLighten40 = brandLighten40,
        ).copy(
            backgroundNorm = ProtonPalette.Haiti,
            interactionWeakNorm = ProtonPalette.Jacarta,
            interactionWeakPressed = ProtonPalette.Valhalla,
            separatorNorm = ProtonPalette.Jacarta,
            textNorm = ProtonPalette.White,
            textWeak = ProtonPalette.CadetBlue,
            iconNorm = ProtonPalette.White,
            iconWeak = ProtonPalette.CadetBlue,
            interactionPressed = ProtonPalette.SanMarino,
        )

        private fun sidebarDark(
            brandDarken40: Color = ProtonPalette.Chambray,
            brandDarken20: Color = ProtonPalette.SanMarino,
            brandNorm: Color = ProtonPalette.CornflowerBlue,
            brandLighten20: Color = ProtonPalette.Portage,
            brandLighten40: Color = ProtonPalette.Perano,
        ) = baseDark(
            brandDarken40 = brandDarken40,
            brandDarken20 = brandDarken20,
            brandNorm = brandNorm,
            brandLighten20 = brandLighten20,
            brandLighten40 = brandLighten40,
        ).copy(
            backgroundNorm = ProtonPalette.Cinder,
            interactionWeakNorm = ProtonPalette.BlackCurrant,
            interactionWeakPressed = ProtonPalette.GunPowder,
            separatorNorm = ProtonPalette.BlackCurrant,
            textNorm = ProtonPalette.White,
            textWeak = ProtonPalette.CadetBlue,
            iconNorm = ProtonPalette.White,
            iconWeak = ProtonPalette.CadetBlue,
            interactionPressed = ProtonPalette.SanMarino,
        )

        fun light(
            brandDarken40: Color = ProtonPalette.Chambray,
            brandDarken20: Color = ProtonPalette.SanMarino,
            brandNorm: Color = ProtonPalette.CornflowerBlue,
            brandLighten20: Color = ProtonPalette.Portage,
            brandLighten40: Color = ProtonPalette.Perano,
        ) = baseLight(
            brandDarken40 = brandDarken40,
            brandDarken20 = brandDarken20,
            brandNorm = brandNorm,
            brandLighten20 = brandLighten20,
            brandLighten40 = brandLighten40,
        ).copy(
            sidebarColors = sidebarLight(
                brandDarken40 = brandDarken40,
                brandDarken20 = brandDarken20,
                brandNorm = brandNorm,
                brandLighten20 = brandLighten20,
                brandLighten40 = brandLighten40,
            )
        )

        fun dark(
            brandDarken40: Color = ProtonPalette.Chambray,
            brandDarken20: Color = ProtonPalette.SanMarino,
            brandNorm: Color = ProtonPalette.CornflowerBlue,
            brandLighten20: Color = ProtonPalette.Portage,
            brandLighten40: Color = ProtonPalette.Perano,
        ) = baseDark(
            brandDarken40 = brandDarken40,
            brandDarken20 = brandDarken20,
            brandNorm = brandNorm,
            brandLighten20 = brandLighten20,
            brandLighten40 = brandLighten40,
        ).copy(
            sidebarColors = sidebarDark(
                brandDarken40 = brandDarken40,
                brandDarken20 = brandDarken20,
                brandNorm = brandNorm,
                brandLighten20 = brandLighten20,
                brandLighten40 = brandLighten40,
            )
        )
    }
}

fun ProtonColors.textNorm(enabled: Boolean = true) = if (enabled) textNorm else textDisabled
fun ProtonColors.textWeak(enabled: Boolean = true) = if (enabled) textWeak else textDisabled
fun ProtonColors.textInverted(enabled: Boolean = true) = if (enabled) textInverted else textDisabled
fun ProtonColors.interactionNorm(enabled: Boolean = true) = if (enabled) interactionNorm else interactionDisabled

internal fun ProtonColors.toMaterial3ThemeColors() = androidx.compose.material3.ColorScheme(
    primary = brandNorm,
    onPrimary = Color.White,
    primaryContainer = backgroundNorm,
    onPrimaryContainer = textNorm,
    inversePrimary = Color.White,
    secondary = brandNorm,
    onSecondary = Color.White,
    secondaryContainer = backgroundSecondary,
    onSecondaryContainer = textNorm,
    tertiary = brandDarken20,
    onTertiary = Color.White,
    tertiaryContainer = backgroundNorm,
    onTertiaryContainer = textNorm,
    background = backgroundNorm,
    onBackground = textNorm,
    surface = backgroundSecondary,
    onSurface = textNorm,
    surfaceVariant = backgroundNorm,
    onSurfaceVariant = textNorm,
    inverseSurface = backgroundNorm,
    inverseOnSurface = textNorm,
    error = notificationError,
    onError = textInverted,
    errorContainer = backgroundNorm,
    onErrorContainer = textNorm,
    outline = brandNorm,
    surfaceTint = Color.Unspecified,
    outlineVariant = brandNorm,
    scrim = brandDarken40
)

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
    shade15 = other.shade15
    shade10 = other.shade10
    shade0 = other.shade0

    textNorm = other.textNorm
    textAccent = other.textAccent
    textWeak = other.textWeak
    textHint = other.textHint
    textDisabled = other.textDisabled
    textInverted = other.textInverted

    iconNorm = other.iconNorm
    iconAccent = other.iconAccent
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
    backgroundDeep = other.backgroundDeep

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

object AccentColors {
    val Purple = ProtonAccentColor(ProtonPalette.PurpleBase)
    val Enzian = ProtonAccentColor(ProtonPalette.EnzianBase)
    val Pink = ProtonAccentColor(ProtonPalette.PinkBase)
    val Plum = ProtonAccentColor(ProtonPalette.PlumBase)
    val Strawberry = ProtonAccentColor(ProtonPalette.StrawberryBase)
    val Cerise = ProtonAccentColor(ProtonPalette.CeriseBase)
    val Carrot = ProtonAccentColor(ProtonPalette.CarrotBase)
    val Copper = ProtonAccentColor(ProtonPalette.CopperBase)
    val Sahara = ProtonAccentColor(ProtonPalette.SaharaBase)
    val Soil = ProtonAccentColor(ProtonPalette.SoilBase)
    val SlateBlue = ProtonAccentColor(ProtonPalette.SlateBlueBase)
    val Cobalt = ProtonAccentColor(ProtonPalette.CobaltBase)
    val Pacific = ProtonAccentColor(ProtonPalette.PacificBase)
    val Ocean = ProtonAccentColor(ProtonPalette.OceanBase)
    val Reef = ProtonAccentColor(ProtonPalette.ReefBase)
    val Pine = ProtonAccentColor(ProtonPalette.PineBase)
    val Fern = ProtonAccentColor(ProtonPalette.FernBase)
    val Forest = ProtonAccentColor(ProtonPalette.ForestBase)
    val Olive = ProtonAccentColor(ProtonPalette.OliveBase)
    val Pickle = ProtonAccentColor(ProtonPalette.PickleBase)
}

data class ProtonAccentColor(
    val base: Color,
    val strong: Color,
    val intense: Color,
) {
    constructor(base: Color) : this(base, base.strongVariant(), base.intenseVariant())
}

fun Color.strongVariant(): Color = Color(strongColorVariant(toArgb()))
fun Color.intenseVariant(): Color = Color(intenseColorVariant(toArgb()))

internal val LocalColors = staticCompositionLocalOf { ProtonColors.Light }
