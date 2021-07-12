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

package me.proton.core.settings.presentation.ui

import android.content.Context
import androidx.fragment.app.FragmentManager
import me.proton.core.presentation.ui.alert.EnterPasswordDialog
import me.proton.core.presentation.utils.inTransaction

private const val TAG_PASSWORD_ENTER_DIALOG = "password_enter_dialog"

/**
 * Presents to the user a dialog to ask for a password (and 2FA code).
 *
 * @param largeLayout how to present the dialog (default false)
 */
fun FragmentManager.showPasswordEnterDialog(
    context: Context,
    largeLayout: Boolean = false,
    twoFA: Boolean = false,
    block: (password: String, secondFactorCode: String) -> Unit
) {
    findFragmentByTag(TAG_PASSWORD_ENTER_DIALOG) ?: run {
        val updateDialogFragment = EnterPasswordDialog(false) { password, twoFA ->
            block(password, twoFA)
        }
        if (largeLayout) {
            // For large screens (tablets), we show the fragment as a dialog
            updateDialogFragment.show(this, TAG_PASSWORD_ENTER_DIALOG)
        } else {
            // The smaller screens (phones), we show the fragment fullscreen
            inTransaction {
                add(updateDialogFragment, TAG_PASSWORD_ENTER_DIALOG)
            }
        }
    }
}
