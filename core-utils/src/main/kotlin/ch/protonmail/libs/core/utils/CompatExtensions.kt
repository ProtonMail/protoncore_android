@file:Suppress("unused") // Public APIs

package ch.protonmail.libs.core.utils

import android.content.Context
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat

/*
 * Compatibility extensions for brevity purpose.
 * E.g. `ContextCompat.getColor(context, colorRes)` -> `context.getColorCompat(colorRes)`
 *
 * Author: Davide Farella
 */

/**
 * @return color [Int]
 * @see ContextCompat.getColor
 */
fun Context.getColorCompat(@ColorRes colorRes: Int) = ContextCompat.getColor(this, colorRes)
