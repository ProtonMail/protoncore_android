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

import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import me.proton.core.presentation.ui.ProtonActivity
import me.proton.core.settings.presentation.R
import me.proton.core.settings.presentation.databinding.ActivityUpdateRecoveryEmailBinding
import me.proton.core.settings.presentation.entity.SettingsInput

@AndroidEntryPoint
class UpdateRecoveryEmailActivity : ProtonActivity<ActivityUpdateRecoveryEmailBinding>() {

    private val input: SettingsInput by lazy {
        requireNotNull(intent?.extras?.getParcelable(ARG_INPUT))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        with(binding.toolbar) {
            title = "Recovery Email"
        }
        supportFragmentManager.showRecoveryEmail(R.id.layoutContent, input)

        supportFragmentManager.setFragmentResultListener(
            UpdateRecoveryEmailFragment.KEY_UPDATE_RESULT, this@UpdateRecoveryEmailActivity
        ) { _, _ ->
            finish()
        }
    }

    override fun layoutId() = R.layout.activity_update_recovery_email

    companion object {
        const val ARG_INPUT = "arg.updateRecoveryEmailInput"
        const val ARG_RESULT = "arg.updateRecoveryEmailResult"
    }
}
