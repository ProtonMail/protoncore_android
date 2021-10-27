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

package me.proton.core.presentation.utils

import android.view.View
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.snackbar.Snackbar
import me.proton.core.presentation.R

/**
 * Shows normal snack bar.
 */
fun View.normSnack(@StringRes messageRes: Int) {
    snack(messageRes = messageRes, color = R.drawable.snackbar_background_norm)
}

/**
 * Shows normal snack bar.
 */
fun View.normSnack(message: String) {
    snack(message = message, color = R.drawable.snackbar_background_norm)
}

/**
 * Shows warning snack bar.
 */
fun View.warningSnack(@StringRes messageRes: Int) {
    snack(messageRes = messageRes, color = R.drawable.snackbar_background_warning)
}

/**
 * Shows warning snack bar.
 */
fun View.warningSnack(message: String) {
    snack(message = message, color = R.drawable.snackbar_background_warning)
}

/**
 * Shows red error snack bar.
 */
fun View.errorSnack(@StringRes messageRes: Int) {
    snack(messageRes = messageRes, color = R.drawable.snackbar_background_error)
}

/**
 * Shows red error snack bar.
 */
fun View.errorSnack(message: String) {
    snack(message = message, color = R.drawable.snackbar_background_error)
}

/**
 * Shows red error snack bar.
 */
fun View.errorSnack(message: String, action: String?, actionOnClick: (() -> Unit)?) {
    snack(
        message = message,
        color = R.drawable.snackbar_background_error,
        action = action,
        actionOnClick = actionOnClick
    )
}

/**
 * Shows green success snack bar.
 */
fun View.successSnack(@StringRes messageRes: Int) {
    snack(messageRes = messageRes, color = R.drawable.snackbar_background_success)
}

/**
 * Shows green success snack bar.
 */
fun View.successSnack(message: String) {
    snack(message = message, color = R.drawable.snackbar_background_success)
}

/**
 * General snack bar util function which takes message and color as config.
 * The default showing length is [Snackbar.LENGTH_LONG].
 *
 * @param messageRes the String resource message id
 */
fun View.snack(
    @StringRes messageRes: Int,
    @DrawableRes color: Int
) {
    snack(message = resources.getString(messageRes), color = color)
}

/**
 * General snack bar util function which takes message, color and length as config.
 * The default showing length is [Snackbar.LENGTH_LONG].
 */
fun View.snack(
    message: String,
    @DrawableRes color: Int,
    action: String? = null,
    actionOnClick: (() -> Unit)? = null,
    length: Int = Snackbar.LENGTH_LONG
) {
    snack(message, color, length) {
        if (action != null && actionOnClick != null) setAction(action) { actionOnClick() }
    }
}

/**
 * General snack bar util function which takes message, color and length and a configuration block.
 * The default showing length is [Snackbar.LENGTH_LONG].
 */
fun View.snack(
    message: String,
    @DrawableRes color: Int,
    length: Int = Snackbar.LENGTH_LONG,
    configBlock: (Snackbar.() -> Unit)? = null
) {
    Snackbar.make(this, message, length).apply {
        view.background = ResourcesCompat.getDrawable(context.resources, color, null)
        view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).apply { maxLines = 5 }
        configBlock?.invoke(this)
    }.show()
}
