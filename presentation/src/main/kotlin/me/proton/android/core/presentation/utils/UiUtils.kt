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

package me.proton.android.core.presentation.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import me.proton.android.core.presentation.R


/**
 * @author Dino Kadrikj.
 */
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

fun AppCompatActivity.hideKeyboard() {
    hideKeyboard(currentFocus ?: window.decorView.rootView)
}

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}
