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

package me.proton.core.auth.presentation.ui

import android.content.Context
import androidx.fragment.app.FragmentManager
import me.proton.core.auth.presentation.R
import me.proton.core.presentation.ui.alert.ProtonCancellableAlertDialog
import me.proton.core.presentation.utils.inTransaction
import me.proton.core.presentation.utils.openBrowserLink

/**
 * @author Dino Kadrikj.
 */

private const val TAG_PASSWORD_CHOOSER_DIALOG = "password_chooser_dialog"

/**
 * Presents to the user a dialog to inform that it should update the password inn order to be able to login.
 * The positive response should bring the user to a web browser.
 *
 * @param largeLayout how to present the dialog (default false)
 */
fun FragmentManager.showPasswordChangeDialog(
    context: Context,
    largeLayout: Boolean = false
) {
    findFragmentByTag(TAG_PASSWORD_CHOOSER_DIALOG) ?: run {
        val updateDialogFragment = ProtonCancellableAlertDialog(
            context.getString(R.string.auth_password_chooser_title),
            context.getString(R.string.auth_password_chooser_description),
            context.getString(R.string.auth_password_chooser_change)
        ) {
            context.openBrowserLink(context.getString(R.string.login_link))
        }
        if (largeLayout) {
            // For large screens (tablets), we show the fragment as a dialog
            updateDialogFragment.show(this, TAG_PASSWORD_CHOOSER_DIALOG)
        } else {
            // The smaller screens (phones), we show the fragment fullscreen
            inTransaction {
                add(updateDialogFragment, TAG_PASSWORD_CHOOSER_DIALOG)
            }
        }
    }
}
