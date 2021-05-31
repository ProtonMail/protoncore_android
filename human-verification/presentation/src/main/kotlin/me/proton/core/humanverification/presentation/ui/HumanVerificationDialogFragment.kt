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

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.humanverification.presentation.R
import me.proton.core.humanverification.presentation.databinding.DialogHumanVerificationMainBinding
import me.proton.core.humanverification.presentation.entity.HumanVerificationResult
import me.proton.core.humanverification.presentation.utils.showEnterCode
import me.proton.core.humanverification.presentation.utils.showHelp
import me.proton.core.humanverification.presentation.utils.showHumanVerificationCaptchaContent
import me.proton.core.humanverification.presentation.utils.showHumanVerificationEmailContent
import me.proton.core.humanverification.presentation.utils.showHumanVerificationSMSContent
import me.proton.core.humanverification.presentation.viewmodel.HumanVerificationViewModel
import me.proton.core.network.domain.session.ClientId
import me.proton.core.network.domain.session.ClientIdType
import me.proton.core.network.domain.session.CookieSessionId
import me.proton.core.network.domain.session.SessionId
import me.proton.core.presentation.ui.ProtonDialogFragment
import me.proton.core.presentation.utils.onClick
import me.proton.core.user.domain.entity.UserVerificationTokenType
import me.proton.core.util.kotlin.exhaustive

/**
 * Shows the dialog for the Human Verification options and option procedures.
 */
@AndroidEntryPoint
class HumanVerificationDialogFragment : ProtonDialogFragment<DialogHumanVerificationMainBinding>() {

    companion object {
        private const val ARG_CLIENT_ID = "arg.clientId"
        private const val ARG_CLIENT_ID_TYPE = "arg.clientIdType"
        private const val ARG_CAPTCHA_TOKEN = "arg.captcha-token"
        private const val ARG_RECOVERY_EMAIL_ADDRESS = "arg.recoveryEmailAddress"
        private const val ARG_CAPTCHA_BASE_URL = "arg.baseUrl"
        const val ARG_VERIFICATION_OPTIONS = "arg.verification-options"
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
            clientId: String,
            captchaBaseUrl: String? = null,
            clientIdType: String,
            availableVerificationMethods: List<String>,
            captchaToken: String?,
            recoveryEmailAddress: String?
        ) = HumanVerificationDialogFragment().apply {
            arguments = bundleOf(
                ARG_CLIENT_ID to clientId,
                ARG_CAPTCHA_BASE_URL to captchaBaseUrl,
                ARG_CLIENT_ID_TYPE to clientIdType,
                ARG_VERIFICATION_OPTIONS to availableVerificationMethods,
                ARG_CAPTCHA_TOKEN to captchaToken,
                ARG_RECOVERY_EMAIL_ADDRESS to recoveryEmailAddress
            )
        }
    }

    private val viewModel by viewModels<HumanVerificationViewModel>()
    private lateinit var resultListener: OnResultListener

    private val clientIdType: ClientIdType by lazy {
        ClientIdType.getByValue(requireArguments().getString(ARG_CLIENT_ID_TYPE, null))
    }

    private val clientId: ClientId by lazy {
        val clientId = requireArguments().getString(ARG_CLIENT_ID)!!
        when (clientIdType) {
            ClientIdType.SESSION -> ClientId.AccountSession(SessionId(clientId))
            ClientIdType.COOKIE -> ClientId.CookieSession(CookieSessionId(clientId))
        }.exhaustive
    }

    private val sessionId: SessionId? by lazy {
        when (clientIdType) {
            ClientIdType.SESSION -> SessionId(clientId.id)
            ClientIdType.COOKIE -> null
        }.exhaustive
    }

    private val captchaToken: String? by lazy {
        requireArguments().get(ARG_CAPTCHA_TOKEN) as String?
    }

    private val captchaBaseUrl: String? by lazy {
        requireArguments().get(ARG_CAPTCHA_BASE_URL) as String?
    }

    private val recoveryEmailAddress: String? by lazy {
        requireArguments().get(ARG_RECOVERY_EMAIL_ADDRESS) as String?
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        childFragmentManager.setFragmentResultListener(KEY_PHASE_TWO, this) { _, bundle ->
            val destination = bundle.getString(ARG_DESTINATION)
            val tokenType = UserVerificationTokenType.fromString(bundle.getString(ARG_TOKEN_TYPE)!!)
            childFragmentManager.showEnterCode(
                sessionId = sessionId,
                tokenType = tokenType,
                destination = destination
            )
        }
        childFragmentManager.setFragmentResultListener(KEY_VERIFICATION_DONE, this) { _, bundle ->
            val tokenCode = bundle.getString(ARG_TOKEN_CODE)
            val tokenType = bundle.getString(ARG_TOKEN_TYPE)
            setResult(tokenType, tokenCode)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        resultListener = context as OnResultListener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.enabledMethods
            .onEach { setEnabledVerificationMethods(it) }
            .launchIn(lifecycleScope)

        viewModel.activeMethod
            .onEach { setActiveVerificationMethod(UserVerificationTokenType.fromString(it)) }
            .launchIn(lifecycleScope)

        binding.headerNavigation.closeButton.onClick {
            setResult(tokenType = null, tokenCode = null, canceled = true)
        }
        binding.headerNavigation.helpButton.onClick {
            childFragmentManager.showHelp()
        }

        binding.verificationOptions.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {}

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    val type = tab.tag as UserVerificationTokenType
                    viewModel.defineActiveVerificationMethod(type)
                }
            }
        })
    }

    override fun layoutId(): Int = R.layout.dialog_human_verification_main

    private fun setEnabledVerificationMethods(enabledMethods: List<String>) {
        binding.verificationOptions.apply {
            for (method in enabledMethods) {
                val tab = newTab().apply {
                    text = method
                    tag = UserVerificationTokenType.fromString(method)
                }
                addTab(tab)
            }
        }
    }

    private fun setActiveVerificationMethod(verificationMethod: UserVerificationTokenType) {
        when (verificationMethod) {
            UserVerificationTokenType.CAPTCHA -> {
                childFragmentManager.showHumanVerificationCaptchaContent(
                    captchaBaseUrl = captchaBaseUrl,
                    token = captchaToken,
                    containerId = binding.fragmentOptionsContainer.id
                )
            }
            UserVerificationTokenType.EMAIL -> {
                childFragmentManager.showHumanVerificationEmailContent(
                    sessionId = sessionId,
                    containerId = binding.fragmentOptionsContainer.id,
                    recoveryEmailAddress = recoveryEmailAddress
                )
            }
            UserVerificationTokenType.SMS -> {
                childFragmentManager.showHumanVerificationSMSContent(
                    sessionId = sessionId,
                    containerId = binding.fragmentOptionsContainer.id
                )
            }
            else -> {
                childFragmentManager.showHelp()
            }
        }
    }

    private fun setResult(tokenType: String? = null, tokenCode: String? = null, canceled: Boolean = false) {
        viewModel.onHumanVerificationSuccess(clientId, tokenType, tokenCode).invokeOnCompletion {
            resultListener.setResult(
                HumanVerificationResult(
                    clientId = clientId.id,
                    clientIdType = sessionId?.let { ClientIdType.SESSION.value } ?: run { ClientIdType.COOKIE.value },
                    tokenType = tokenType, tokenCode = tokenCode, canceled = canceled
                )
            )
        }
    }

    override fun onBackPressed() {
        with(childFragmentManager) {
            if (backStackEntryCount >= 1) {
                popBackStack()
            } else {
                viewModel.onHumanVerificationCanceled(clientId).invokeOnCompletion {
                    resultListener.setResult(
                        HumanVerificationResult(
                            clientId = clientId.id,
                            clientIdType = sessionId?.let { ClientIdType.SESSION.value }
                                ?: run { ClientIdType.COOKIE.value },
                            tokenType = null,
                            tokenCode = null,
                            canceled = true
                        )
                    )
                    dismissAllowingStateLoss()
                }
            }
        }
    }

    interface OnResultListener {
        fun setResult(result: HumanVerificationResult?)
    }
}
