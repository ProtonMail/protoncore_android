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

package me.proton.core.humanverification.presentation.ui

import android.os.Bundle
import android.view.View
import me.proton.core.humanverification.presentation.R
import me.proton.core.humanverification.presentation.databinding.FragmentHumanVerificationHelpBinding
import me.proton.core.presentation.ui.ProtonDialogFragment
import me.proton.core.presentation.utils.onClick
import me.proton.core.presentation.utils.openBrowserLink

class HumanVerificationHelpFragment :
    ProtonDialogFragment<FragmentHumanVerificationHelpBinding>() {

    override fun layoutId(): Int = R.layout.fragment_human_verification_help

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            binding.toolbar.setNavigationOnClickListener {
                onBackPressed()
            }
            verificationManual.manualVerificationLayout.onClick {
                requireContext().openBrowserLink(getString(R.string.manual_verification_link))
            }
            verificationHelp.helpLayout.onClick {
                requireContext().openBrowserLink(getString(R.string.verification_help_link))
            }
        }
    }

    override fun onBackPressed() {
        dismissAllowingStateLoss()
    }
}
