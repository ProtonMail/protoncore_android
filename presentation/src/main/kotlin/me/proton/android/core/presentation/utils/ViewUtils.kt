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

@file:Suppress("unused") // Public APIs

package me.proton.android.core.presentation.utils

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.AdapterView
import android.widget.EditText
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import me.proton.android.core.presentation.R

/**
 * Shortcut for [AdapterView.setOnItemSelectedListener]
 * This allow us to pass a simple lambda, instead of an anonymous class of
 * [AdapterView.OnItemSelectedListener]
 *
 * @param block takes *position* [Int]
 */
inline fun <T : Adapter> AdapterView<T>.onItemSelected(crossinline block: (position: Int) -> Unit) {
    onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

        /**
         * Callback method to be invoked when an item in this view has been
         * selected. This callback is invoked only when the newly selected
         * position is different from the previously selected position or if
         * there was no selected item.
         *
         * Implementers can call getItemAtPosition(position) if they need to access the
         * data associated with the selected item.
         *
         * @param parent The AdapterView where the selection happened
         * @param view The view within the AdapterView that was clicked
         * @param position The position of the view in the adapter
         * @param id The row id of the item that is selected
         */
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            block(position)
        }

        /**
         * Callback method to be invoked when the selection disappears from this
         * view. The selection can disappear for instance when touch is activated
         * or when the adapter becomes empty.
         *
         * @param parent The AdapterView that now contains no selected item.
         */
        override fun onNothingSelected(parent: AdapterView<*>?) {
            // Noop
        }
    }
}

/**
 * Clears the edit text content.
 */
fun EditText.clearText() = setText("")

/** Execute the [listener] on [TextWatcher.onTextChanged] */
inline fun EditText.onTextChange(crossinline listener: (CharSequence) -> Unit): TextWatcher {
    val watcher = object : TextWatcher {
        override fun afterTextChanged(editable: Editable) {
            /* Do nothing */
        }

        override fun beforeTextChanged(text: CharSequence, start: Int, count: Int, after: Int) {
            /* Do nothing */
        }

        override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
            listener(text)
        }
    }
    addTextChangedListener(watcher)
    return watcher
}

/**
 * Shortcut for [View.setOnClickListener]
 * This also allow us to pass a function KProperty as argument
 * e.g. `` view.onClick(::doSomething) ``
 */
inline fun View.onClick(crossinline block: () -> Unit) {
    setOnClickListener { block() }
}

/**
 * Inflate a [LayoutRes] from the receiver [ViewGroup]
 * @param attachToRoot Default is `false`
 * @return [View]
 */
fun ViewGroup.inflate(@LayoutRes layoutId: Int, attachToRoot: Boolean = false): View =
    LayoutInflater.from(context).inflate(layoutId, this, attachToRoot)

/**
 * Shows red error snack bar. Usually as a general way to display various errors to the user.
 *
 * @param messageRes the String resource error message id
 */
fun View.errorSnack(@StringRes messageRes: Int) {
    snack(messageRes = messageRes, color = R.drawable.background_error)
}

/**
 * Shows red error snack bar. Usually as a general way to display various errors to the user.
 */
fun View.errorSnack(message: String) {
    snack(message = message, color = R.drawable.background_error)
}

/**
 * Shows green success snack bar. Usually as a general way to display success result of an operation to the user.
 */
fun View.successSnack(@StringRes messageRes: Int) {
    snack(messageRes = messageRes, color = R.drawable.background_success)
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
 *
 * @param message the message as String
 */
fun View.snack(
    message: String,
    length: Int = Snackbar.LENGTH_LONG,
    @DrawableRes color: Int
) {
    Snackbar.make(this, message, length).apply {
        view.background = context.resources.getDrawable(color, null)
        setTextColor(ContextCompat.getColor(context, R.color.text_light))
    }.show()
}
