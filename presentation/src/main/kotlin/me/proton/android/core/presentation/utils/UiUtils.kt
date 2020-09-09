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

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.snackbar.Snackbar
import me.proton.android.core.presentation.R

/**
 * @author Dino Kadrikj.
 */

inline fun FragmentManager.inTransaction(block: FragmentTransaction.() -> FragmentTransaction) {
    val transaction = beginTransaction()
    transaction.block()
    transaction.commit()
}

fun Context.openLinkInBrowser(link: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
    intent.resolveActivity(packageManager)?.let {
        startActivity(intent)
    } ?: Toast.makeText(
        this,
        getString(R.string.presentation_browser_missing),
        Toast.LENGTH_SHORT
    ).show()
}

fun View.errorSnack(@StringRes messageRes: Int) {
    snack(messageRes = messageRes, color = R.drawable.background_error)
}

fun View.successSnack(@StringRes messageRes: Int) {
    snack(messageRes = messageRes, color = R.drawable.background_success)
}

private fun View.snack(
    @StringRes messageRes: Int,
    @DrawableRes color: Int
) {
    snack(message = resources.getString(messageRes), color = color)
}

private fun View.snack(
    message: String,
    length: Int = Snackbar.LENGTH_LONG,
    @DrawableRes color: Int
) {
    Snackbar.make(this, message, length).apply {
        view.background = context.resources.getDrawable(color, null)
        setTextColor(ContextCompat.getColor(context, R.color.text_light))
    }.show()
}


