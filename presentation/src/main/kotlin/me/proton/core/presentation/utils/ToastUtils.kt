/*
 * Copyright (c) 2020 Proton Technologies AG
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

@file:JvmName("TextUtils")

package me.proton.core.presentation.utils

import android.content.Context
import android.view.Gravity
import android.widget.Toast
import androidx.annotation.StringRes

/*
 * A file containing extensions for Text
 * Author: Davide Farella
 */

private const val DEFAULT_TOAST_LENGTH = Toast.LENGTH_LONG
private const val DEFAULT_TOAST_GRAVITY = Gravity.BOTTOM

/**
 * An extension for show a [Toast] within a [Context]
 * @param messageRes [StringRes] of message to show
 * @param length [Int] length of the [Toast]. Default is [DEFAULT_TOAST_LENGTH]
 * @param gravity [Int] gravity for the [Toast]. Default is [DEFAULT_TOAST_GRAVITY]
 */
@JvmOverloads
fun Context.showToast(
    @StringRes messageRes: Int,
    length: Int = DEFAULT_TOAST_LENGTH,
    gravity: Int = DEFAULT_TOAST_GRAVITY
) {
    @Suppress("SENSELESS_COMPARISON") // It could be `null` if called from Java
    if (this != null) {
        Toast.makeText(this, messageRes, length).apply {
            setGravity(gravity, 0, 0)
        }.show()
    }
}

/**
 * An extension for show a [Toast] within a [Context]
 * @param message [CharSequence] message to show
 * @param length [Int] length of the [Toast]. Default is [DEFAULT_TOAST_LENGTH]
 * @param gravity [Int] gravity for the [Toast]. Default is [DEFAULT_TOAST_GRAVITY]
 */
@JvmOverloads
fun Context.showToast(
    message: CharSequence,
    length: Int = DEFAULT_TOAST_LENGTH,
    gravity: Int = DEFAULT_TOAST_GRAVITY
) {
    @Suppress("SENSELESS_COMPARISON") // It could be `null` if called from Java
    if (this != null) {
        Toast.makeText(this, message, length).apply {
            setGravity(gravity, 0, 0)
        }.show()
    }
}
