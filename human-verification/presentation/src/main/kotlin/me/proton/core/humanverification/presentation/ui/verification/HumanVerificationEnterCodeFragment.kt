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
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import me.proton.core.humanverification.domain.entity.TokenType
import me.proton.core.humanverification.presentation.R
import me.proton.core.humanverification.presentation.databinding.FragmentHumanVerificationEnterCodeBinding
import me.proton.core.humanverification.presentation.ui.HumanVerificationDialogFragment
import me.proton.core.humanverification.presentation.ui.HumanVerificationDialogFragment.Companion.ARG_TOKEN_CODE
import me.proton.core.humanverification.presentation.ui.HumanVerificationDialogFragment.Companion.KEY_VERIFICATION_DONE
import me.proton.core.humanverification.presentation.utils.showHelp
import me.proton.core.humanverification.presentation.viewmodel.verification.HumanVerificationEnterCodeViewModel
import me.proton.core.network.domain.session.SessionId
import me.proton.core.presentation.ui.ProtonDialogFragment
import me.proton.core.presentation.utils.errorSnack
import me.proton.core.presentation.utils.onClick
import me.proton.core.presentation.utils.onFailure
import me.proton.core.presentation.utils.onSuccess
import me.proton.core.presentation.utils.successSnack
import me.proton.core.presentation.utils.validate
import me.proton.core.presentation.viewmodel.onError
import me.proton.core.presentation.viewmodel.onSuccess

/**
 * @author Dino Kadrikj.
 */
@AndroidEntryPoint
class HumanVerificationEnterCodeFragment : ProtonDialogFragment<FragmentHumanVerificationEnterCodeBinding>() {

    private val viewModel by viewModels<HumanVerificationEnterCodeViewModel>()

    private val sessionId: SessionId by lazy {
        SessionId(requireArguments().getString(ARG_SESSION_ID)!!)
    }

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
                binding.headerNavigation.closeButton.setIconResource(R.drawable.ic_arrow_back)
                onClick { onBackPressed() }
            }
            headerNavigation.helpButton.onClick { childFragmentManager.showHelp() }
            verifyButton.onClick {
                verificationCodeEditText.validate()
                    .onFailure { verificationCodeEditText.setInputError() }
                    .onSuccess {
                        parentFragmentManager.setFragmentResult(
                            KEY_VERIFICATION_DONE,
                            bundleOf(
                                ARG_TOKEN_CODE to it,
                                HumanVerificationDialogFragment.ARG_TOKEN_TYPE to tokenType.tokenTypeValue
                            )
                        )
                    }
            }
            requestReplacementButton.onClick { viewModel.resendCode(sessionId) }
        }

        viewModel.codeVerificationResult.onSuccess {
            parentFragmentManager.setFragmentResult(
                KEY_VERIFICATION_DONE,
                bundleOf(
                    ARG_TOKEN_CODE to binding.verificationCodeEditText.text,
                    HumanVerificationDialogFragment.ARG_TOKEN_TYPE to tokenType.tokenTypeValue
                )
            )
        }.onError {
            showErrorCode()
        }.launchIn(lifecycleScope)

        viewModel.verificationCodeResendStatus.onSuccess {
            showCodeResent()
        }.launchIn(lifecycleScope)
    }

    override fun layoutId(): Int = R.layout.fragment_human_verification_enter_code

    private fun showErrorCode() {
        binding.verifyButton.setIdle()
        view?.errorSnack(R.string.human_verification_incorrect_code)
    }

    private fun showCodeResent() {
        binding.verifyButton.setIdle()
        view?.successSnack(R.string.human_verification_resent_code)
    }

    override fun onBackPressed() {
        dismissAllowingStateLoss()
    }

    companion object {
        private const val ARG_SESSION_ID = "arg.sessionId"
        private const val ARG_DESTINATION = "arg.destination"
        private const val ARG_TOKEN_TYPE = "arg.enter-code-token-type"

        operator fun invoke(
            sessionId: String,
            tokenType: TokenType,
            destination: String?
        ) = HumanVerificationEnterCodeFragment().apply {
            arguments = bundleOf(
                ARG_SESSION_ID to sessionId,
                ARG_DESTINATION to destination,
                ARG_TOKEN_TYPE to tokenType.tokenTypeValue
            )
        }
    }
}
