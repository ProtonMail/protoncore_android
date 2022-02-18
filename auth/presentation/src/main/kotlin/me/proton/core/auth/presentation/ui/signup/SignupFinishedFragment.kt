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
import me.proton.core.auth.presentation.R
import me.proton.core.auth.presentation.databinding.FragmentSignupFinishedBinding
import me.proton.core.domain.entity.Product
import me.proton.core.presentation.utils.onClick
import me.proton.core.presentation.utils.viewBinding
import me.proton.core.util.kotlin.exhaustive

class SignupFinishedFragment : SignupFragment(R.layout.fragment_signup_finished) {

    private val binding by viewBinding(FragmentSignupFinishedBinding::bind)

    private val product: Product? by lazy {
        val arguments = requireArguments()
        val ordinal = arguments.getInt(ChooseUsernameFragment.ARG_PRODUCT, -1)
        Product.values().getOrNull(ordinal)
    }

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
        private const val ARG_PRODUCT = "arg.product"

        operator fun invoke(product: Product?) = SignupFinishedFragment().apply {
            arguments = bundleOf(
                ARG_PRODUCT to product?.ordinal
            )
        }
    }
}
