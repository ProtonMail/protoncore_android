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
import dagger.hilt.android.AndroidEntryPoint
import me.proton.android.core.presentation.ui.ProtonFragment
import me.proton.android.core.presentation.utils.errorSnack
import me.proton.android.core.presentation.utils.onClick
import me.proton.android.core.presentation.utils.onFailure
import me.proton.android.core.presentation.utils.onSuccess
import me.proton.android.core.presentation.utils.validateEmail
import me.proton.core.humanverification.domain.entity.TokenType
import me.proton.core.humanverification.presentation.R
import me.proton.core.humanverification.presentation.databinding.FragmentHumanVerificationEmailBinding
import me.proton.core.humanverification.presentation.ui.verification.HumanVerificationMethodCommon.Companion.ARG_URL_TOKEN
import me.proton.core.humanverification.presentation.viewmodel.verification.HumanVerificationEmailViewModel
import me.proton.core.network.domain.session.SessionId

/**
 * Fragment that handles human verification with email address.
 *
 * @author Dino Kadrikj.
 */
@AndroidEntryPoint
internal class HumanVerificationEmailFragment : ProtonFragment<FragmentHumanVerificationEmailBinding>() {

    companion object {
        private const val ARG_SESSION_ID = "arg.sessionId"
        private const val ARG_RECOVERY_EMAIL = "arg.recoveryemail"

        operator fun invoke(
            sessionId: String,
            token: String,
            recoveryEmailAddress: String? = null
        ) = HumanVerificationEmailFragment().apply {
            arguments = bundleOf(
                ARG_SESSION_ID to sessionId,
                ARG_URL_TOKEN to token,
                ARG_RECOVERY_EMAIL to recoveryEmailAddress
            )
        }
    }

    private val viewModel by viewModels<HumanVerificationEmailViewModel>()

    private val humanVerificationBase by lazy {
        HumanVerificationMethodCommon(
            viewModel = viewModel,
            urlToken = requireArguments().get(ARG_URL_TOKEN) as String,
            tokenType = TokenType.EMAIL
        )
    }

    private val sessionId: SessionId by lazy {
        SessionId(requireArguments().getString(ARG_SESSION_ID)!!)
    }

    private val recoveryEmailAddress: String? by lazy {
        requireArguments().get(ARG_RECOVERY_EMAIL) as String?
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        humanVerificationBase.onViewCreated(
            owner = viewLifecycleOwner,
            parentFragmentManager = parentFragmentManager,
            loadable = binding.getVerificationCodeButton,
            onVerificationCodeError = ::onError
        )

        recoveryEmailAddress?.let {
            binding.emailEditText.text = it
        }
        binding.apply {
            getVerificationCodeButton.onClick {
                emailEditText.validateEmail()
                    .onFailure { emailEditText.setInputError() }
                    .onSuccess {
                        getVerificationCodeButton.setLoading()
                        viewModel.sendVerificationCode(sessionId, it)
                    }
            }
            proceedButton.onClick {
                humanVerificationBase.onGetCodeClicked(parentFragmentManager)
            }
        }
        viewModel.validation.observe(viewLifecycleOwner) {
            doOnError {
                binding.emailEditText.setInputError()
            }
        }
    }

    override fun layoutId(): Int = R.layout.fragment_human_verification_email

    private fun onError() = with(binding) {
        root.errorSnack(R.string.human_verification_sending_failed)
    }
}
