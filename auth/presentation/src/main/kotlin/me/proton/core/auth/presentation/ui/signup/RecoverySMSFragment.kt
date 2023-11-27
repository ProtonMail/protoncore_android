/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and ProtonCore.
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
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.auth.presentation.R
import me.proton.core.auth.presentation.databinding.FragmentRecoverySmsBinding
import me.proton.core.auth.presentation.entity.signup.RecoveryMethodType
import me.proton.core.auth.presentation.viewmodel.signup.RecoveryMethodViewModel
import me.proton.core.auth.presentation.viewmodel.signup.RecoverySMSViewModel
import me.proton.core.country.presentation.entity.CountryUIModel
import me.proton.core.country.presentation.ui.CountryPickerFragment
import me.proton.core.country.presentation.ui.showCountryPicker
import me.proton.core.presentation.ui.ProtonFragment
import me.proton.core.presentation.utils.onTextChange
import me.proton.core.presentation.utils.viewBinding
import me.proton.core.presentation.viewmodel.onSuccess
import me.proton.core.telemetry.presentation.annotation.ProductMetrics
import me.proton.core.telemetry.presentation.annotation.ViewClicked
import me.proton.core.telemetry.presentation.annotation.ViewFocused

@ProductMetrics(group = "account.any.signup", flow = "mobile_signup_full")
@ViewClicked(
    "user.recovery_method.clicked",
    viewIds = ["phone_country"]
)
@ViewFocused(
    "user.recovery_method.focused",
    viewIds = ["phone"]
)
@AndroidEntryPoint
class RecoverySMSFragment : ProtonFragment(R.layout.fragment_recovery_sms) {

    private val viewModel by viewModels<RecoverySMSViewModel>()
    private val recoveryMethodViewModel: RecoveryMethodViewModel by viewModels({ requireParentFragment() })
    private val binding by viewBinding(FragmentRecoverySmsBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getCountryCallingCode()

        childFragmentManager.setFragmentResultListener(CountryPickerFragment.KEY_COUNTRY_SELECTED, this) { _, bundle ->
            val country = bundle.getParcelable<CountryUIModel>(CountryPickerFragment.BUNDLE_KEY_COUNTRY)
            binding.phoneCountry.text = "+${country?.callingCode}"
        }

        binding.apply {
            phoneCountry.setOnClickListener {
                childFragmentManager.showCountryPicker()
            }

            with(phone) {
                onTextChange(
                    afterTextChangeListener = { editable ->
                        recoveryMethodViewModel.setActiveRecoveryMethod(
                            userSelectedMethodType = RecoveryMethodType.SMS,
                            destination = "${phoneCountry.text}${editable}"
                        )
                    }
                )
            }
        }

        viewModel.countryCallingCode.onSuccess {
            binding.phoneCountry.text =
                String.format(getString(R.string.presentation_phone_calling_code_template), it)
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        recoveryMethodViewModel.validationResult.onEach {
            when (it) {
                is RecoveryMethodViewModel.ValidationState.Success,
                is RecoveryMethodViewModel.ValidationState.Skipped -> binding.phone.flush()
                else -> {
                    // no operation
                }
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }
}
