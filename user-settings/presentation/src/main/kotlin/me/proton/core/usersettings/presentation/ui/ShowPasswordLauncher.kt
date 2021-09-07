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

package me.proton.core.usersettings.presentation.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import me.proton.core.auth.presentation.alert.PasswordAnd2FADialog
import me.proton.core.auth.presentation.alert.showPasswordEnterDialog
import me.proton.core.auth.presentation.entity.PasswordAnd2FAInput
import me.proton.core.presentation.ui.alert.FragmentDialogResultLauncher

data class ShowPasswordInput(
    val showPassword: Boolean,
    val showTwoFA: Boolean
)

fun FragmentManager.registerShowPasswordDialogResultLauncher(
    fragment: Fragment,
    onResultLoginPassword: ((PasswordAnd2FAInput?) -> Unit)? = null,
    onResultMailboxPassword: ((PasswordAnd2FAInput?) -> Unit)? = null,
): FragmentDialogResultLauncher<ShowPasswordInput> {

    setFragmentResultListener(
        PasswordAnd2FADialog.KEY_PASS_2FA_SET,
        fragment
    ) { _, bundle ->
        val result =
            bundle.getParcelable<PasswordAnd2FAInput>(PasswordAnd2FADialog.BUNDLE_KEY_PASS_2FA_DATA)
        if (onResultLoginPassword != null) {
            onResultLoginPassword(result)
        }
        if (onResultMailboxPassword != null) {
            onResultMailboxPassword(result)
        }
    }

    return FragmentDialogResultLauncher(
        requestKey = PasswordAnd2FADialog.KEY_PASS_2FA_SET,
        show = { input ->
            requireNotNull(input) { "Input must not be null" }
            showPasswordEnterDialog(password = input.showPassword, secondFactor = input.showTwoFA)
        }
    )
}
