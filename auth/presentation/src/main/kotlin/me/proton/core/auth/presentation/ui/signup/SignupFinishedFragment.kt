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

package me.proton.core.auth.presentation.ui.signup

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import dagger.hilt.android.AndroidEntryPoint
import me.proton.core.auth.presentation.R
import me.proton.core.auth.presentation.databinding.FragmentSignupFinishedBinding
import me.proton.core.domain.entity.Product
import me.proton.core.presentation.utils.onClick
import me.proton.core.presentation.utils.viewBinding
import me.proton.core.util.kotlin.exhaustive
import javax.inject.Inject

@AndroidEntryPoint
class SignupFinishedFragment : SignupFragment(R.layout.fragment_signup_finished) {

    private val binding by viewBinding(FragmentSignupFinishedBinding::bind)

    @Inject
    lateinit var product: Product

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.startUsingProton.apply {
            text = getString(
                when (product) {
                    Product.Calendar -> R.string.auth_signup_start_using_calendar
                    Product.Drive -> R.string.auth_signup_start_using_drive
                    Product.Mail -> R.string.auth_signup_start_using_mail
                    Product.Vpn -> R.string.auth_signup_start_using_vpn
                    null -> R.string.auth_signup_start_using_proton
                }.exhaustive
            )
            onClick {
                onBackPressed()
            }
        }
        binding.congratsView.setImageResource(
            when (product) {
                Product.Calendar -> R.drawable.ic_congratulations_calendar
                Product.Drive,
                Product.Mail,
                Product.Vpn,
                null -> R.drawable.ic_congratulations_mail
            }.exhaustive
        )
    }

    override fun onBackPressed() {
        parentFragmentManager.setFragmentResult(
            KEY_START_USING_SELECTED, bundleOf()
        )
    }

    companion object {
        const val KEY_START_USING_SELECTED = "key.start_using_selected"

        operator fun invoke() = SignupFinishedFragment()
    }
}
