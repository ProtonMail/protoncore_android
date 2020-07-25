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

package me.proton.core.humanverification.presentation.ui.verification

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import me.proton.android.core.presentation.ui.ProtonFragment
import me.proton.android.core.presentation.utils.clearText
import me.proton.android.core.presentation.utils.onClick
import me.proton.core.humanverification.domain.entity.TokenType
import me.proton.core.humanverification.presentation.R
import me.proton.core.humanverification.presentation.databinding.FragmentHumanVerificationSmsBinding
import me.proton.core.humanverification.presentation.entity.CountryUIModel
import me.proton.core.humanverification.presentation.ui.verification.HumanVerificationMethodCommon.Companion.ARG_URL_TOKEN
import me.proton.core.humanverification.presentation.utils.errorSnack
import me.proton.core.humanverification.presentation.utils.removeInputError
import me.proton.core.humanverification.presentation.utils.setInputError
import me.proton.core.humanverification.presentation.utils.showCountryPicker
import me.proton.core.humanverification.presentation.utils.validate
import me.proton.core.humanverification.presentation.viewmodel.verification.HumanVerificationSMSViewModel

/**
 * Fragment that handles human verification with phone number.
 *
 * @author Dino Kadrikj.
 */
@AndroidEntryPoint
internal class HumanVerificationSMSFragment :
    ProtonFragment<FragmentHumanVerificationSmsBinding>() {

    companion object {
        const val KEY_COUNTRY_SELECTED = "key.country_selected"
        const val BUNDLE_KEY_COUNTRY = "bundle.country"

        operator fun invoke(token: String): HumanVerificationSMSFragment =
            HumanVerificationSMSFragment().apply {
                val args = bundleOf(ARG_URL_TOKEN to token)
                if (arguments != null) requireArguments().putAll(args)
                else arguments = args
            }
    }

    private val viewModel by viewModels<HumanVerificationSMSViewModel>()

    private val humanVerificationBase by lazy {
        HumanVerificationMethodCommon(
            viewModel = viewModel,
            urlToken = requireArguments().get(ARG_URL_TOKEN) as String,
            tokenType = TokenType.SMS
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        humanVerificationBase.onViewCreated(
            owner = viewLifecycleOwner,
            parentFragmentManager = parentFragmentManager,
            onVerificationCodeError = ::hideProgressAndEnableResend
        )

        childFragmentManager.setFragmentResultListener(KEY_COUNTRY_SELECTED, this) { _, bundle ->
            val country = bundle.getParcelable<CountryUIModel>(BUNDLE_KEY_COUNTRY)
            binding.callingCodeText.text = "+${country?.callingCode}"
        }

        binding.apply {
            phoneClearButton.onClick { smsEditText.clearText() }

            callingCodeText.setOnClickListener {
                childFragmentManager.showCountryPicker()
            }

            getVerificationCodeButton.setOnClickListener {
                smsEditText.text.toString().validate({ binding.smsEditText.setInputError() }, {
                    showProgressAndDisableResend()
                    viewModel.sendVerificationCodeToDestination(
                        countryCallingCode = callingCodeText.text.toString(), // this is not expected to be empty
                        phoneNumber = it
                    )
                })
            }

            smsEditText.addTextChangedListener {
                it?.let {
                    if (it.isNotEmpty()) {
                        smsEditText.removeInputError()
                    }
                }
            }

            proceedButton.onClick {
                humanVerificationBase.onGetCodeClicked(parentFragmentManager)
            }
        }

        viewModel.validation.observe(viewLifecycleOwner) {
            doOnError { onValidationError() }
        }
        viewModel.mostUsedCallingCode.observe(viewLifecycleOwner) {
            doOnData {
                binding.callingCodeText.text =
                    String.format(getString(R.string.human_verification_calling_code_template), it)
            }
        }
    }

    override fun layoutId(): Int = R.layout.fragment_human_verification_sms

    private fun onValidationError() {
        hideProgressAndEnableResend()
        binding.smsEditText.setInputError()
    }

    private fun showProgressAndDisableResend() = with(binding) {
        progress.visibility = View.VISIBLE
        getVerificationCodeButton.isEnabled = false
    }

    private fun hideProgressAndEnableResend() = with(binding) {
        progress.visibility = View.GONE
        getVerificationCodeButton.isEnabled = true
        root.errorSnack(R.string.human_verification_sending_failed)
    }
}
