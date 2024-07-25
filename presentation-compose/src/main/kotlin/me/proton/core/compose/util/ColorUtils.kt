/*
 * Copyright (c) 2024 Proton Technologies AG
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

package me.proton.core.compose.util

import android.content.Context
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.graphics.ColorUtils

object ProtonColorUtils {
    /** Converts ARGB color in argb format (0xFF...) into HSL array. */
    fun rgbToHsl(@ColorInt color: Int): FloatArray {
        val hslArray = FloatArray(3)
        ColorUtils.RGBToHSL(Color.red(color), Color.green(color), Color.blue(color), hslArray)
        return hslArray
    }

    /** Returns a strong color variant for any given color in ARGB format (0xFF...). */
    fun strongColorVariant(@ColorInt color: Int): Int {
        val hsl = rgbToHsl(color)
        val hslConversion = floatArrayOf(
            hsl[0],
            hsl[1] - 0.05f,
            hsl[2] - 0.05f,
        )
        return ColorUtils.HSLToColor(hslConversion)
    }

    /** Returns an intense color variant for any given color in ARGB format (0xFF...). */
    fun intenseColorVariant(@ColorInt color: Int): Int {
        val hsl = rgbToHsl(color)
        val hslConversion = floatArrayOf(
            hsl[0],
            hsl[1] - 0.10f,
            hsl[2] - 0.10f,
        )
        return ColorUtils.HSLToColor(hslConversion)
    }
}

data class ProtonAccentColorCompat(
    val base: Int,
    val strong: Int,
    val intense: Int,
) {
    /** Creates a [ProtonAccentColorCompat] instance from an ARGB color value (0xFF...). */
    constructor(@ColorInt base: Int) : this(
        base,
        ProtonColorUtils.strongColorVariant(base),
        ProtonColorUtils.intenseColorVariant(base)
    )

    companion object {
        /** Creates a [ProtonAccentColorCompat] instance from a color resource. */
        fun fromColorResource(context: Context, @ColorRes baseId: Int): ProtonAccentColorCompat {
            val base = context.getColor(baseId)
            val strong = ProtonColorUtils.strongColorVariant(base)
            val intense = ProtonColorUtils.intenseColorVariant(base)
            return ProtonAccentColorCompat(base, strong, intense)
        }
    }
}
