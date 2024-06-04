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
import me.proton.core.auth.presentation.alert.ConfirmPasswordInputDialog
import me.proton.core.auth.presentation.alert.TwoFAInputDialog
import me.proton.core.auth.presentation.alert.showPasswordEnterDialog
import me.proton.core.auth.presentation.alert.showTwoFAEnterDialog
import me.proton.core.auth.presentation.entity.PasswordInput
import me.proton.core.auth.presentation.entity.TwoFAInput
import me.proton.core.domain.entity.UserId
import me.proton.core.presentation.ui.alert.FragmentDialogResultLauncher

fun FragmentManager.registerShowPasswordDialogResultLauncher(
    fragment: Fragment,
    onResultLoginPassword: ((PasswordInput?) -> Unit)? = null,
    onResultMailboxPassword: ((PasswordInput?) -> Unit)? = null,
): FragmentDialogResultLauncher<Unit> {

    setFragmentResultListener(
        ConfirmPasswordInputDialog.KEY_PASS_SET,
        fragment
    ) { _, bundle ->
        val result =
            bundle.getParcelable<PasswordInput>(ConfirmPasswordInputDialog.BUNDLE_KEY_PASS_DATA)
        if (onResultLoginPassword != null) {
            onResultLoginPassword(result)
        }
        if (onResultMailboxPassword != null) {
            onResultMailboxPassword(result)
        }
    }

    return FragmentDialogResultLauncher(
        requestKey = ConfirmPasswordInputDialog.KEY_PASS_SET,
        show = { showPasswordEnterDialog() }
    )
}

fun FragmentManager.registerShowTwoFADialogResultLauncher(
    fragment: Fragment,
    userId: UserId,
    onResult: (TwoFAInput?) -> Unit
): FragmentDialogResultLauncher<Unit> {

    setFragmentResultListener(
        TwoFAInputDialog.KEY_2FA_SET,
        fragment
    ) { _, bundle ->
        val result =
            bundle.getParcelable<TwoFAInput>(TwoFAInputDialog.BUNDLE_KEY_2FA_DATA)
        onResult(result)
    }

    return FragmentDialogResultLauncher(
        requestKey = TwoFAInputDialog.KEY_2FA_SET,
        show = { showTwoFAEnterDialog(userId = userId) }
    )
}
