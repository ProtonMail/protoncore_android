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

import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import me.proton.core.presentation.ui.ProtonActivity
import me.proton.core.usersettings.presentation.R
import me.proton.core.usersettings.presentation.databinding.ActivityPasswordManagementBinding
import me.proton.core.usersettings.presentation.entity.SettingsInput
import me.proton.core.usersettings.presentation.ui.PasswordManagementFragment.Companion.KEY_UPDATE_RESULT

@AndroidEntryPoint
class PasswordManagementActivity : ProtonActivity<ActivityPasswordManagementBinding>() {

    private val input: SettingsInput by lazy {
        requireNotNull(intent?.extras?.getParcelable(ARG_INPUT))
    }

    override fun layoutId() = R.layout.activity_password_management

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        with(binding.toolbar) {
            title = getString(R.string.settings_password_management_header)
        }
        supportFragmentManager.showUpdatePassword(R.id.layoutContent, input)

        supportFragmentManager.setFragmentResultListener(
            KEY_UPDATE_RESULT, this@PasswordManagementActivity
        ) { _, _ ->
            finish()
        }
    }

    companion object {
        const val ARG_INPUT = "arg.updatePasswordInput"
        const val ARG_RESULT = "arg.updatePasswordResult"
    }
}