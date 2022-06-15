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

import androidx.fragment.app.FragmentManager
import me.proton.core.presentation.utils.inTransaction
import me.proton.core.usersettings.presentation.entity.SettingsInput

private const val TAG_RECOVERY_EMAIL = "recovery_fragment"
private const val TAG_UPDATE_PASSWORD = "update_password_fragment"

fun FragmentManager.showRecoveryEmail(
    containerId: Int = android.R.id.content,
    input: SettingsInput
) {
    findFragmentByTag(TAG_RECOVERY_EMAIL) ?: run {
        val updateRecoveryEmailFragment = UpdateRecoveryEmailFragment(input)
        inTransaction {
            setCustomAnimations(0, 0)
            replace(containerId, updateRecoveryEmailFragment, TAG_RECOVERY_EMAIL)
            addToBackStack(TAG_RECOVERY_EMAIL)
        }
    }
}

fun FragmentManager.showUpdatePassword(
    containerId: Int = android.R.id.content,
    input: SettingsInput
) {
    findFragmentByTag(TAG_UPDATE_PASSWORD) ?: run {
        val updatePasswordFragment = PasswordManagementFragment(input)
        inTransaction {
            setCustomAnimations(0, 0)
            replace(containerId, updatePasswordFragment, TAG_UPDATE_PASSWORD)
            addToBackStack(TAG_UPDATE_PASSWORD)
        }
    }
}
