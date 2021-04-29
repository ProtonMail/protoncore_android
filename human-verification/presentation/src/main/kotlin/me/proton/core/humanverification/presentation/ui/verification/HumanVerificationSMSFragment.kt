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
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import me.proton.core.country.presentation.entity.CountryUIModel
import me.proton.core.country.presentation.ui.CountryPickerFragment.Companion.BUNDLE_KEY_COUNTRY
import me.proton.core.country.presentation.ui.CountryPickerFragment.Companion.KEY_COUNTRY_SELECTED
import me.proton.core.country.presentation.ui.showCountryPicker
import me.proton.core.humanverification.presentation.R
import me.proton.core.humanverification.presentation.databinding.FragmentHumanVerificationSmsBinding
import me.proton.core.humanverification.presentation.ui.verification.HumanVerificationMethodCommon.Companion.ARG_URL_TOKEN
import me.proton.core.humanverification.presentation.viewmodel.verification.HumanVerificationSMSViewModel
import me.proton.core.network.domain.session.SessionId
import me.proton.core.presentation.ui.ProtonFragment
import me.proton.core.presentation.utils.errorSnack
import me.proton.core.presentation.utils.hideKeyboard
import me.proton.core.presentation.utils.onClick
import me.proton.core.presentation.utils.onFailure
import me.proton.core.presentation.utils.onSuccess
import me.proton.core.presentation.utils.validate
import me.proton.core.presentation.viewmodel.onError
import me.proton.core.presentation.viewmodel.onSuccess
import me.proton.core.user.domain.entity.UserVerificationTokenType

/**
 * Fragment that handles human verification with phone number.
 *
 * @author Dino Kadrikj.
 */
@AndroidEntryPoint
internal class HumanVerificationSMSFragment :
    ProtonFragment<FragmentHumanVerificationSmsBinding>() {

    companion object {
        private const val ARG_SESSION_ID = "arg.sessionId"

        operator fun invoke(
            sessionId: String?,
            token: String
        ) = HumanVerificationSMSFragment().apply {
            arguments = bundleOf(
                ARG_SESSION_ID to sessionId,
                ARG_URL_TOKEN to token
            )
        }
    }

    private val viewModel by viewModels<HumanVerificationSMSViewModel>()

    private val sessionId: SessionId? by lazy {
        requireArguments().getString(ARG_SESSION_ID)?.let {
            SessionId(it)
        } ?: run {
            null
        }
    }

    private val humanVerificationBase by lazy {
        HumanVerificationMethodCommon(
            viewModel = viewModel,
            urlToken = requireArguments().get(ARG_URL_TOKEN) as String,
            tokenType = UserVerificationTokenType.SMS
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getMostUsedCallingCode()
        humanVerificationBase.onViewCreated(
            owner = viewLifecycleOwner,
            parentFragmentManager = parentFragmentManager,
            loadable = binding.getVerificationCodeButton,
            onVerificationCodeError = ::onError
        )

        childFragmentManager.setFragmentResultListener(KEY_COUNTRY_SELECTED, this) { _, bundle ->
            val country = bundle.getParcelable<CountryUIModel>(BUNDLE_KEY_COUNTRY)
            binding.callingCodeText.text = "+${country?.callingCode}"
        }

        binding.apply {
            callingCodeText.setOnClickListener {
                childFragmentManager.showCountryPicker()
            }

            getVerificationCodeButton.onClick {
                hideKeyboard()
                getVerificationCodeButton.setLoading()
                smsEditText.validate()
                    .onFailure {
                        binding.smsEditText.setInputError()
                        getVerificationCodeButton.setIdle()
                    }
                    .onSuccess {
                        viewModel.sendVerificationCodeToDestination(
                            sessionId = sessionId,
                            countryCallingCode = callingCodeText.text.toString(), // this is not expected to be empty
                            phoneNumber = it
                        )
                    }
            }

            proceedButton.onClick {
                hideKeyboard()
                humanVerificationBase.onGetCodeClicked(parentFragmentManager = parentFragmentManager)
            }
        }

        viewModel.validation.onError {
            onValidationError()
        }.launchIn(lifecycleScope)

        viewModel.mostUsedCallingCode.onSuccess {
            binding.callingCodeText.text =
                String.format(getString(R.string.human_verification_calling_code_template), it)
        }.launchIn(lifecycleScope)
    }

    override fun layoutId(): Int = R.layout.fragment_human_verification_sms

    private fun onValidationError() {
        onError()
        binding.smsEditText.setInputError()
    }

    private fun onError() = with(binding) {
        root.errorSnack(R.string.human_verification_sending_failed)
    }
}
