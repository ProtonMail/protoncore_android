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

import android.text.util.Linkify
import android.view.View
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.snackbar.Snackbar
import me.proton.core.presentation.R

enum class SnackType(@DrawableRes val background: Int) {
    Success(R.drawable.snackbar_background_success),
    Norm(R.drawable.snackbar_background_norm),
    Warning(R.drawable.snackbar_background_warning),
    Error(R.drawable.snackbar_background_error)
}

/**
 * Shows normal snack bar.
 */
fun View.normSnack(
    @StringRes messageRes: Int,
    length: Int = Snackbar.LENGTH_LONG,
    configBlock: (Snackbar.() -> Unit)? = null,
) =
    snack(messageRes = messageRes, type = SnackType.Norm, length = length, configBlock = configBlock)

/**
 * Shows normal snack bar.
 */
fun View.normSnack(message: String, length: Int = Snackbar.LENGTH_LONG, configBlock: (Snackbar.() -> Unit)? = null) =
    snack(message = message, type = SnackType.Norm, length = length, configBlock = configBlock)

/**
 * Shows warning snack bar.
 */
fun View.warningSnack(
    @StringRes messageRes: Int,
    length: Int = Snackbar.LENGTH_LONG,
    configBlock: (Snackbar.() -> Unit)? = null,
) =
    snack(messageRes = messageRes, type = SnackType.Warning, length = length, configBlock = configBlock)

/**
 * Shows warning snack bar.
 */
fun View.warningSnack(message: String, length: Int = Snackbar.LENGTH_LONG, configBlock: (Snackbar.() -> Unit)? = null,) =
    snack(message = message, type = SnackType.Warning, length = length, configBlock = configBlock)

/**
 * Shows red error snack bar.
 */
fun View.errorSnack(
    @StringRes messageRes: Int,
    length: Int = Snackbar.LENGTH_LONG,
    configBlock: (Snackbar.() -> Unit)? = null,
) =
    snack(messageRes = messageRes, type = SnackType.Error, length = length, configBlock = configBlock)

/**
 * Shows red error snack bar.
 */
fun View.errorSnack(message: String, length: Int = Snackbar.LENGTH_LONG, configBlock: (Snackbar.() -> Unit)? = null) =
    snack(message = message, type = SnackType.Error, length = length, configBlock = configBlock)

/**
 * Shows red error snack bar.
 */
fun View.errorSnack(
    message: String,
    action: String?,
    actionOnClick: (() -> Unit)?,
    length: Int = Snackbar.LENGTH_LONG,
    configBlock: (Snackbar.() -> Unit)? = null,
) = snack(
    message = message,
    type = SnackType.Error,
    action = action,
    actionOnClick = actionOnClick,
    length = length,
    configBlock = configBlock,
)

/**
 * Shows green success snack bar.
 */
fun View.successSnack(
    @StringRes messageRes: Int,
    length: Int = Snackbar.LENGTH_LONG,
    configBlock: (Snackbar.() -> Unit)? = null,
) =
    snack(messageRes = messageRes, type = SnackType.Success, length = length, configBlock = configBlock)

/**
 * Shows green success snack bar.
 */
fun View.successSnack(message: String, length: Int = Snackbar.LENGTH_LONG, configBlock: (Snackbar.() -> Unit)? = null) =
    snack(message = message, type = SnackType.Success, length = length, configBlock = configBlock)

/**
 * General snack bar util function which takes message and type as config.
 * The default showing length is [Snackbar.LENGTH_LONG].
 *
 * @param messageRes the String resource message id
 */
fun View.snack(
    @StringRes messageRes: Int,
    type: SnackType,
    length: Int = Snackbar.LENGTH_LONG,
    configBlock: (Snackbar.() -> Unit)? = null,
) = snack(message = resources.getString(messageRes), type = type, length = length, configBlock = configBlock)

/**
 * General snack bar util function which takes message and color as config.
 * The default showing length is [Snackbar.LENGTH_LONG].
 *
 * @param messageRes the String resource message id
 */
@Deprecated("Use snack() with type instead of color", ReplaceWith("snack(messageRes, type)"))
@Suppress("deprecation")
fun View.snack(
    @StringRes messageRes: Int,
    @DrawableRes color: Int?,
    configBlock: (Snackbar.() -> Unit)? = null,
) = snack(message = resources.getString(messageRes), color = color, configBlock = configBlock)

/**
 * General snack bar util function which takes message, color and length as config.
 * The default showing length is [Snackbar.LENGTH_LONG].
 */
fun View.snack(
    message: String,
    type: SnackType,
    action: String? = null,
    actionOnClick: (() -> Unit)? = null,
    length: Int = Snackbar.LENGTH_LONG,
    configBlock: (Snackbar.() -> Unit)? = null,
) = snack(message, type, length) {
    if (action != null && actionOnClick != null) setAction(action) { actionOnClick() }
    configBlock?.invoke(this)
}

@Deprecated(
    "Use snack() with type instead of color",
    ReplaceWith("snack(message, type, action, actionOnClick, length")
)
@Suppress("deprecation")
fun View.snack(
    message: String,
    @DrawableRes color: Int?,
    action: String? = null,
    actionOnClick: (() -> Unit)? = null,
    length: Int = Snackbar.LENGTH_LONG,
    configBlock: (Snackbar.() -> Unit)? = null,
) = snack(message, color, length) {
    if (action != null && actionOnClick != null) setAction(action) { actionOnClick() }
    configBlock?.invoke(this)
}

/**
 * General snack bar util function which takes message, type and length and a configuration block.
 * The default showing length is [Snackbar.LENGTH_LONG].
 */
@Suppress("deprecation")
fun View.snack(
    message: String,
    type: SnackType,
    length: Int = Snackbar.LENGTH_LONG,
    configBlock: (Snackbar.() -> Unit)? = null,
) = snack(message, type.background, length, configBlock)

@Deprecated(
    "Use snack() with type instead of color",
    ReplaceWith("snack(message, type, length, configBlock)")
)
fun View.snack(
    message: String,
    @DrawableRes color: Int?,
    length: Int = Snackbar.LENGTH_LONG,
    configBlock: (Snackbar.() -> Unit)? = null,
): Snackbar {
    return Snackbar.make(this, message, length).apply {
        color?.let { view.background = ResourcesCompat.getDrawable(context.resources, it, null) }
        view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).apply {
            maxLines = 5
            autoLinkMask = Linkify.WEB_URLS
            movementMethod = android.text.method.LinkMovementMethod.getInstance()
        }
        configBlock?.invoke(this)
    }.also { it.show() }
}
