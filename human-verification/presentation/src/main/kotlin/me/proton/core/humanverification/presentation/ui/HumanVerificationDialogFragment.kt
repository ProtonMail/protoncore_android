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
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import me.proton.android.core.presentation.ui.ProtonDialogFragment
import me.proton.android.core.presentation.utils.onClick
import me.proton.core.humanverification.domain.entity.TokenType
import me.proton.core.humanverification.presentation.R
import me.proton.core.humanverification.presentation.databinding.DialogHumanVerificationMainBinding
import me.proton.core.humanverification.presentation.utils.showEnterCode
import me.proton.core.humanverification.presentation.utils.showHelp
import me.proton.core.humanverification.presentation.utils.showHumanVerificationCaptchaContent
import me.proton.core.humanverification.presentation.utils.showHumanVerificationEmailContent
import me.proton.core.humanverification.presentation.utils.showHumanVerificationSMSContent
import me.proton.core.humanverification.presentation.viewmodel.HumanVerificationViewModel
import me.proton.core.humanverification.presentation.viewmodel.HumanVerificationViewModelFactory

/**
 * Shows the dialog for the Human Verification options and option procedures.
 *
 * @author Dino Kadrikj.
 */
@AndroidEntryPoint
class HumanVerificationDialogFragment :
    ProtonDialogFragment<DialogHumanVerificationMainBinding>() {

    companion object {
        private const val ARG_VERIFICATION_OPTIONS = "arg.verification-options"
        private const val ARG_CAPTCHA_TOKEN = "arg.captcha-token"
        const val ARG_DESTINATION = "arg.destination"
        const val ARG_TOKEN_CODE = "arg.token-code"
        const val ARG_TOKEN_TYPE = "arg.token-type"
        const val KEY_PHASE_TWO = "key.phase_two"
        const val KEY_VERIFICATION_DONE = "key.verification_done"

        /**
         * The only verification method (type) that is receiving aa token from the 9001 human
         * verification response is [TokenType.CAPTCHA] and should be passed to the constructor.
         *
         * @param availableVerificationMethods all available verification methods, returned from the API
         * @param captchaToken if the API returns it, otherwise null
         */
        operator fun invoke(
            availableVerificationMethods: List<String>,
            captchaToken: String?
        ): HumanVerificationDialogFragment =
            HumanVerificationDialogFragment().apply {
                val args = bundleOf(
                    ARG_VERIFICATION_OPTIONS to availableVerificationMethods,
                    ARG_CAPTCHA_TOKEN to captchaToken
                )
                if (arguments != null) requireArguments().putAll(args)
                else arguments = args
            }
    }

    private val viewModel by viewModels<HumanVerificationViewModel> {
        HumanVerificationViewModelFactory(requireArguments().get(ARG_VERIFICATION_OPTIONS) as List<String>)
    }

    private val captchaToken: String? by lazy {
        requireArguments().get(ARG_CAPTCHA_TOKEN) as String?
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        childFragmentManager.setFragmentResultListener(KEY_PHASE_TWO, this) { _, bundle ->
            val destination = bundle.getString(ARG_DESTINATION)
            val tokenType = TokenType.fromString(bundle.getString(ARG_TOKEN_TYPE)!!)
            childFragmentManager.showEnterCode(
                tokenType = tokenType,
                destination = destination
            )
        }
        childFragmentManager.setFragmentResultListener(KEY_VERIFICATION_DONE, this) { _, bundle ->
            val tokenCode = bundle.getString(ARG_TOKEN_CODE)
            val tokenType = bundle.getString(ARG_TOKEN_TYPE)
            onClose(tokenType, tokenCode)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.enabledMethods.observe(viewLifecycleOwner) {
            doOnData { setEnabledVerificationMethods(it) }
        }
        viewModel.activeMethod.observe(viewLifecycleOwner) {
            doOnData { setActiveVerificationMethod(TokenType.fromString(it)) }
        }
        binding.headerNavigation.closeButton.onClick(::onClose)
        binding.headerNavigation.helpButton.onClick {
            childFragmentManager.showHelp()
        }

        binding.verificationOptions.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {}

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    val type = tab.tag as TokenType
                    viewModel.defineActiveVerificationMethod(type)
                }
            }
        })
    }

    override fun getStyleResource(): Int = R.style.ProtonTheme_Dialog_Picker

    override fun layoutId(): Int = R.layout.dialog_human_verification_main

    private fun setEnabledVerificationMethods(enabledMethods: List<String>) {
        binding.verificationOptions.apply {
            for (method in enabledMethods) {
                val tab = newTab().apply {
                    text = method
                    tag = TokenType.fromString(method)
                }
                addTab(tab)
            }
        }
    }

    private fun setActiveVerificationMethod(verificationMethod: TokenType) {
        when (verificationMethod) {
            TokenType.CAPTCHA -> {
                childFragmentManager.showHumanVerificationCaptchaContent(
                    token = captchaToken,
                    containerId = binding.fragmentOptionsContainer.id
                )
            }
            TokenType.EMAIL -> {
                childFragmentManager.showHumanVerificationEmailContent(
                    containerId = binding.fragmentOptionsContainer.id
                )
            }
            TokenType.SMS -> {
                childFragmentManager.showHumanVerificationSMSContent(
                    containerId = binding.fragmentOptionsContainer.id
                )
            }
            else -> {
                childFragmentManager.showHelp()
            }
        }
    }

    private fun onClose(tokenType: String? = null, tokenCode: String? = null) {
        if (!tokenType.isNullOrEmpty() && !tokenCode.isNullOrEmpty()) {
            parentFragmentManager.setFragmentResult(
                KEY_VERIFICATION_DONE,
                bundleOf(
                    ARG_TOKEN_CODE to tokenCode,
                    ARG_TOKEN_TYPE to tokenType
                )
            )
            dismissAllowingStateLoss()
            return
        }
        onBackPressed()
    }

    override fun onBackPressed() {
        with(childFragmentManager) {
            if (backStackEntryCount >= 1) {
                popBackStack()
            } else {
                dismissAllowingStateLoss()
            }
        }
    }
}
