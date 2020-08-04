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

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import me.proton.android.core.presentation.ui.ProtonDialogFragment
import me.proton.android.core.presentation.utils.onClick
import me.proton.core.humanverification.domain.entity.TokenType
import me.proton.core.humanverification.presentation.R
import me.proton.core.humanverification.presentation.databinding.FragmentHumanVerificationEnterCodeBinding
import me.proton.core.humanverification.presentation.ui.HumanVerificationDialogFragment
import me.proton.core.humanverification.presentation.ui.HumanVerificationDialogFragment.Companion.ARG_TOKEN_CODE
import me.proton.core.humanverification.presentation.ui.HumanVerificationDialogFragment.Companion.KEY_VERIFICATION_DONE
import me.proton.core.humanverification.presentation.utils.errorSnack
import me.proton.core.humanverification.presentation.utils.removeInputError
import me.proton.core.humanverification.presentation.utils.setInputError
import me.proton.core.humanverification.presentation.utils.showHelp
import me.proton.core.humanverification.presentation.utils.successSnack
import me.proton.core.humanverification.presentation.utils.validate
import me.proton.core.humanverification.presentation.viewmodel.verification.HumanVerificationEnterCodeViewModel

/**
 * @author Dino Kadrikj.
 */
@AndroidEntryPoint
class HumanVerificationEnterCodeFragment :
    ProtonDialogFragment<FragmentHumanVerificationEnterCodeBinding>() {

    companion object {
        private const val ARG_DESTINATION = "arg.destination"
        private const val ARG_TOKEN_TYPE = "arg.enter-code-token-type"

        operator fun invoke(
            tokenType: TokenType,
            destination: String?
        ): HumanVerificationEnterCodeFragment =
            HumanVerificationEnterCodeFragment().apply {
                val args =
                    bundleOf(
                        ARG_DESTINATION to destination,
                        ARG_TOKEN_TYPE to tokenType.tokenTypeValue
                    )
                if (arguments != null) requireArguments().putAll(args)
                else arguments = args
            }
    }

    private val viewModel by viewModels<HumanVerificationEnterCodeViewModel>()

    private val destination: String? by lazy {
        val value = requireArguments().get(ARG_DESTINATION) as String?
        viewModel.destination = value
        value
    }

    private val tokenType: TokenType by lazy {
        val type = requireArguments().getString(ARG_TOKEN_TYPE)
        val value = TokenType.fromString(type)
        viewModel.tokenType = value
        value
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        destination?.let {
            binding.title.text = String.format(
                getString(R.string.human_verification_enter_code_subtitle),
                destination
            )
        } ?: run {
            binding.title.text = getString(R.string.human_verification_enter_code_subtitle_already_have_code)
        }

        binding.apply {
            // this should go inside the custom edit text input view (validation also with error text below the view)
            headerNavigation.closeButton.apply {
                binding.headerNavigation.closeButton.setImageResource(R.drawable.ic_arrow_left)
                onClick { onBackPressed() }
            }
            headerNavigation.helpButton.onClick { childFragmentManager.showHelp() }
            verificationCodeEditText.addTextChangedListener {
                it?.let {
                    if (it.isNotEmpty()) {
                        verificationCodeEditText.removeInputError()
                    }
                }
            }
            verifyButton.onClick {
                verificationCodeEditText.text.toString()
                    .validate({ verificationCodeEditText.setInputError() }, {
                        viewModel.verificationComplete(tokenType, it)
                        parentFragmentManager.setFragmentResult(
                            KEY_VERIFICATION_DONE,
                            bundleOf(
                                ARG_TOKEN_CODE to it,
                                HumanVerificationDialogFragment.ARG_TOKEN_TYPE to tokenType.tokenTypeValue
                            )
                        )
                    })
            }
            requestReplacementButton.onClick { viewModel.resendCode() }
        }

        viewModel.codeVerificationResult.observe(viewLifecycleOwner) {
            doOnData {
                parentFragmentManager.setFragmentResult(
                    KEY_VERIFICATION_DONE,
                    bundleOf(
                        ARG_TOKEN_CODE to binding.verificationCodeEditText.text.toString(),
                        HumanVerificationDialogFragment.ARG_TOKEN_TYPE to tokenType.tokenTypeValue
                    )
                )
            }
            doOnError { showErrorCode() }
        }

        viewModel.verificationCodeResendStatus.observe(viewLifecycleOwner) {
            doOnData { showCodeResent() }
        }
    }

    override fun layoutId(): Int = R.layout.fragment_human_verification_enter_code

    private fun showErrorCode() {
        showProgress(false)
        view?.errorSnack(R.string.human_verification_incorrect_code)
    }

    private fun showCodeResent() {
        showProgress(false)
        view?.successSnack(R.string.human_verification_resent_code)
    }

    private fun showProgress(show: Boolean) = with(binding) {
        progress.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun getStyleResource(): Int = R.style.ProtonTheme_Dialog_Picker

    override fun onBackPressed() {
        dismissAllowingStateLoss()
    }
}
