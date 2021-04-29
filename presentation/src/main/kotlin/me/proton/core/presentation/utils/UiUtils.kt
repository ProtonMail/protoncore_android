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

package me.proton.core.presentation.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import me.proton.core.presentation.R
import me.proton.core.presentation.ui.alert.ForceUpdateDialog

private const val TAG_FORCE_UPDATE_DIALOG = "force_update_dialog"

inline fun FragmentManager.inTransaction(block: FragmentTransaction.() -> FragmentTransaction) {
    val transaction = beginTransaction()
    transaction.block()
    transaction.commit()
}

fun Context.openBrowserLink(link: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
    intent.resolveActivity(packageManager)?.let {
        startActivity(intent)
    } ?: run {
        Toast.makeText(
            this,
            getString(R.string.presentation_browser_missing),
            Toast.LENGTH_SHORT
        ).show()
    }
}

fun Context.openMarketLink() {
    val uri = Uri.parse("market://details?id=$packageName")
    val storeIntent = Intent(Intent.ACTION_VIEW, uri).apply {
        addFlags(
            Intent.FLAG_ACTIVITY_NO_HISTORY or
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        )
    }
    storeIntent.resolveActivity(packageManager)?.let {
        startActivity(storeIntent)
    } ?: run {
        Toast.makeText(
            this,
            getString(R.string.presentation_market_missing),
            Toast.LENGTH_SHORT
        ).show()
    }
}

fun AppCompatActivity.hideKeyboard() {
    hideKeyboard(currentFocus ?: window.decorView.rootView)
}

fun Fragment.hideKeyboard() {
    val activity = requireActivity()
    val focus = activity.currentFocus
    focus?.clearFocus()
    activity.hideKeyboard(focus ?: activity.window.decorView.rootView)
}

/**
 * @return true if current [Configuration] UiMode is in Night Mode.
 *
 * Note: This differ from the current System's default ([AppCompatDelegate.getDefaultNightMode]).
 *
 * @see <a href="https://developer.android.com/guide/topics/ui/look-and-feel/darktheme">DarkTheme</a>
 */
fun AppCompatActivity.isNightMode() =
    resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    view.clearFocus()
}

/**
 * Presents to the user non dismissable dialog to inform that the current version of the application is no longer
 * supported.
 *
 * @param apiErrorMessage the error message returned from the API that triggered force update required error.
 * @param learnMoreURL an option if the client want's to override the default URL.
 * @param largeLayout how to present the dialog (default false)
 */
fun FragmentManager.showForceUpdate(
    apiErrorMessage: String,
    learnMoreURL: String? = null,
    largeLayout: Boolean = false
) {
    findFragmentByTag(TAG_FORCE_UPDATE_DIALOG) ?: run {
        val updateDialogFragment = ForceUpdateDialog(apiErrorMessage, learnMoreURL)
        if (largeLayout) {
            // For large screens (tablets), we show the fragment as a dialog
            updateDialogFragment.show(this, TAG_FORCE_UPDATE_DIALOG)
        } else {
            // The smaller screens (phones), we show the fragment fullscreen
            inTransaction {
                add(updateDialogFragment, TAG_FORCE_UPDATE_DIALOG)
            }
        }
    }
}
