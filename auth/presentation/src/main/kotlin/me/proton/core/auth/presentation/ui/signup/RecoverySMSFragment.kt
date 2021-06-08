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
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
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
import me.proton.core.presentation.viewmodel.onSuccess

@AndroidEntryPoint
class RecoverySMSFragment : ProtonFragment<FragmentRecoverySmsBinding>() {

    private val viewModel by viewModels<RecoverySMSViewModel>()
    private val recoveryMethodViewModel: RecoveryMethodViewModel by viewModels({ requireParentFragment() })

    override fun layoutId() = R.layout.fragment_recovery_sms

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getCountryCallingCode()

        childFragmentManager.setFragmentResultListener(CountryPickerFragment.KEY_COUNTRY_SELECTED, this) { _, bundle ->
            val country = bundle.getParcelable<CountryUIModel>(CountryPickerFragment.BUNDLE_KEY_COUNTRY)
            binding.callingCodeText.text = "+${country?.callingCode}"
        }

        binding.apply {
            callingCodeText.setOnClickListener {
                childFragmentManager.showCountryPicker()
            }

            smsEditText.onTextChange(
                afterTextChangeListener = { editable ->
                    recoveryMethodViewModel.setActiveRecoveryMethod(
                        RecoveryMethodType.SMS,
                        "$callingCodeText${editable}"
                    )
                }
            )
        }

        viewModel.countryCallingCode.onSuccess {
            binding.callingCodeText.text =
                String.format(getString(R.string.auth_signup_calling_code_template), it)
        }.launchIn(lifecycleScope)
    }
}
