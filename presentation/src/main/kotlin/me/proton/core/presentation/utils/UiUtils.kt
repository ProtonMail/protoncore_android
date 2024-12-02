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
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import me.proton.core.presentation.R

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
    when (storeIntent.resolveActivity(packageManager)) {
        null -> openBrowserLink("https://play.google.com/store/apps/details?id=$packageName")
        else -> startActivity(storeIntent)
    }
}

fun Context.openMarketSubscription(purchasedProductId: String?) {
    val uri = when (purchasedProductId) {
        null -> Uri.parse("https://play.google.com/store/account/subscriptions")
        else -> Uri.parse(getString(R.string.play_store_app_subscription, purchasedProductId, packageName))
    }
    val storeIntent = Intent(Intent.ACTION_VIEW, uri).apply {
        addFlags(
            Intent.FLAG_ACTIVITY_NO_HISTORY or
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        )
    }
    when (storeIntent.resolveActivity(packageManager)) {
        // we will open app market details if we can not open any deeplink, should be very rare case
        null -> openBrowserLink("https://play.google.com/store/apps/details?id=$packageName")
        else -> startActivity(storeIntent)
    }
}

fun FragmentActivity.hideKeyboard() {
    val focus = currentFocus
    focus?.clearFocus()
    hideKeyboard(currentFocus ?: window.decorView.rootView)
}

fun Fragment.hideKeyboard() = requireContext().hideKeyboard(requireView())

/**
 * @return true if current [Configuration] UiMode is in Night Mode.
 *
 * Note: This differ from the current System's default ([AppCompatDelegate.getDefaultNightMode]).
 *
 * @see <a href="https://developer.android.com/guide/topics/ui/look-and-feel/darktheme">DarkTheme</a>
 */
fun Context.isNightMode() =
    resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

/**
 * Changes the foreground color of the status bars to dark so that the items on the bar can be read clearly.
 */
fun AppCompatActivity.setDarkStatusBar() {
    WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
}

/**
 * Changes the foreground color of the status bars to light so that the items on the bar can be read clearly.
 */
fun AppCompatActivity.setLightStatusBar() {
    WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
}

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    view.clearFocus()
}

fun Context.showKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    if (view.requestFocus()) inputMethodManager.showSoftInput(view, 0)
}
