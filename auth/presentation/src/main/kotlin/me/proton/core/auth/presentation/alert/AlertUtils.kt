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

package me.proton.core.auth.presentation.alert

import androidx.activity.ComponentActivity
import androidx.fragment.app.FragmentManager
import me.proton.core.auth.presentation.alert.confirmpass.ConfirmPasswordDialog
import me.proton.core.auth.presentation.entity.confirmpass.ConfirmPasswordInput
import me.proton.core.auth.presentation.entity.confirmpass.ConfirmPasswordResult
import me.proton.core.domain.entity.UserId
import me.proton.core.presentation.ui.alert.FragmentDialogResultLauncher
import me.proton.core.presentation.utils.inTransaction

private const val TAG_PASSWORD_ENTER_DIALOG = "password_enter_dialog"
private const val TAG_TWO_FA_ENTER_DIALOG = "twofa_enter_dialog"
private const val TAG_CONFIRM_PASSWORD_DIALOG = "confirm_password_dialog"

/**
 * Presents to the user a dialog to ask for a password confirmation.
 *
 * @param largeLayout how to present the dialog (default false)
 */
fun FragmentManager.showPasswordEnterDialog(
    largeLayout: Boolean = false
) {
    findFragmentByTag(TAG_PASSWORD_ENTER_DIALOG) ?: run {
        val fragment = ConfirmPasswordInputDialog()
        if (largeLayout) {
            // For large screens (tablets), we show the fragment as a dialog
            fragment.show(this, TAG_PASSWORD_ENTER_DIALOG)
        } else {
            // The smaller screens (phones), we show the fragment fullscreen
            inTransaction {
                add(fragment, TAG_PASSWORD_ENTER_DIALOG)
            }
        }
    }
}

/**
 * Presents to the user a dialog to ask for a 2FA code).
 *
 * @param largeLayout how to present the dialog (default false)
 */
fun FragmentManager.showTwoFAEnterDialog(
    largeLayout: Boolean = false,
    userId: UserId
) {
    findFragmentByTag(TAG_TWO_FA_ENTER_DIALOG) ?: run {
        val fragment = TwoFAInputDialog(userId.id)
        if (largeLayout) {
            // For large screens (tablets), we show the fragment as a dialog
            fragment.show(this, TAG_TWO_FA_ENTER_DIALOG)
        } else {
            // The smaller screens (phones), we show the fragment fullscreen
            inTransaction {
                add(fragment, TAG_TWO_FA_ENTER_DIALOG)
            }
        }
    }
}

/**
 * Presents to the user a dialog to ask for a password (and 2FA code).
 *
 * @param largeLayout how to present the dialog (default false)
 */
fun FragmentManager.showConfirmPasswordDialog(
    largeLayout: Boolean = false,
    userId: String,
    missingScopes: List<String>
) {
    findFragmentByTag(TAG_CONFIRM_PASSWORD_DIALOG) ?: run {
        val fragment = ConfirmPasswordDialog(
            ConfirmPasswordInput(userId = userId, missingScopes = missingScopes)
        )
        if (largeLayout) {
            // For large screens (tablets), we show the fragment as a dialog
            fragment.show(this, TAG_CONFIRM_PASSWORD_DIALOG)
        } else {
            // The smaller screens (phones), we show the fragment fullscreen
            inTransaction {
                add(fragment, TAG_CONFIRM_PASSWORD_DIALOG)
            }
        }
    }
}

fun FragmentManager.registerConfirmPasswordResultLauncher(
    context: ComponentActivity,
    onResult: ((ConfirmPasswordResult?) -> Unit)? = null,
): FragmentDialogResultLauncher<ConfirmPasswordInput> {

    setFragmentResultListener(
        ConfirmPasswordDialog.CONFIRM_PASS_SET,
        context
    ) { _, bundle ->
        val result =
            bundle.getParcelable<ConfirmPasswordResult>(ConfirmPasswordDialog.BUNDLE_CONFIRM_PASS_DATA)
        if (onResult != null) {
            onResult(result)
        }
    }

    return FragmentDialogResultLauncher(
        requestKey = ConfirmPasswordDialog.CONFIRM_PASS_SET,
        show = { input ->
            showConfirmPasswordDialog(userId = input.userId, missingScopes = input.missingScopes)
        }
    )
}
