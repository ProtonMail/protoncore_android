@file:JvmName("TextUtils")

package ch.protonmail.libs.core.utils

import android.content.Context
import android.view.Gravity
import android.widget.Toast
import androidx.annotation.StringRes
import studio.forface.viewstatestore.ViewState

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

/**
 * An extension for show a [Toast] within a [Context]
 * @param error [ViewState.Error] containing the message to show
 * @param length [Int] length of the [Toast]. Default is [DEFAULT_TOAST_LENGTH]
 * @param gravity [Int] gravity for the [Toast]. Default is [DEFAULT_TOAST_GRAVITY]
 */
@JvmOverloads
fun Context.showToast(
    error: ViewState.Error,
    length: Int = DEFAULT_TOAST_LENGTH,
    gravity: Int = DEFAULT_TOAST_GRAVITY
) {
    showToast(error.getMessage(this), length = length, gravity = gravity)
}
