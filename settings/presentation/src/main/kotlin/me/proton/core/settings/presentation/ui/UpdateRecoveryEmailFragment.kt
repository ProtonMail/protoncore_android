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
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import me.proton.core.presentation.ui.ProtonFragment
import me.proton.core.settings.presentation.R
import me.proton.core.settings.presentation.databinding.FragmentUpdateRecoveryEmailBinding
import me.proton.core.settings.presentation.viewmodel.UpdateRecoveryEmailViewModel

@AndroidEntryPoint
class UpdateRecoveryEmailFragment : ProtonFragment<FragmentUpdateRecoveryEmailBinding>() {

    private val viewModel by viewModels<UpdateRecoveryEmailViewModel>()

    override fun layoutId() = R.layout.fragment_update_recovery_email

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    activity?.finish()
                }
            }
        )
    }
}
