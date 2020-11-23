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
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import me.proton.core.humanverification.domain.entity.TokenType
import me.proton.core.humanverification.presentation.R
import me.proton.core.humanverification.presentation.databinding.FragmentHumanVerificationCaptchaBinding
import me.proton.core.humanverification.presentation.ui.HumanVerificationDialogFragment
import me.proton.core.humanverification.presentation.ui.verification.HumanVerificationMethodCommon.Companion.ARG_URL_TOKEN
import me.proton.core.humanverification.presentation.viewmodel.verification.HumanVerificationCaptchaViewModel
import me.proton.core.network.domain.session.SessionId
import me.proton.core.presentation.ui.ProtonFragment
import me.proton.core.presentation.utils.errorSnack

/**
 * Fragment that handles human verification with Captcha.
 *
 * @author Dino Kadrikj.
 */
@AndroidEntryPoint
internal class HumanVerificationCaptchaFragment : ProtonFragment<FragmentHumanVerificationCaptchaBinding>() {

    companion object {
        private const val ARG_SESSION_ID = "arg.sessionId"
        private const val ARG_HOST = "arg.host"
        private const val MAX_PROGRESS = 100

        operator fun invoke(
            sessionId: String,
            urlToken: String,
            host: String
        ) = HumanVerificationCaptchaFragment().apply {
            arguments = bundleOf(
                ARG_SESSION_ID to sessionId,
                ARG_URL_TOKEN to urlToken,
                ARG_HOST to host
            )
        }
    }

    private val sessionId: SessionId by lazy {
        SessionId(requireArguments().getString(ARG_SESSION_ID)!!)
    }

    private val host: String by lazy {
        requireArguments().get(ARG_HOST) as String
    }
    private val viewModel by viewModels<HumanVerificationCaptchaViewModel>()

    private val humanVerificationBase by lazy {
        HumanVerificationMethodCommon(
            viewModel = viewModel,
            urlToken = requireArguments().get(ARG_URL_TOKEN) as String,
            tokenType = TokenType.CAPTCHA
        )
    }

    override fun layoutId(): Int = R.layout.fragment_human_verification_captcha

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        humanVerificationBase.onViewCreated(viewLifecycleOwner, parentFragmentManager) {
            binding.root.errorSnack(R.string.human_verification_sending_failed)
        }

        viewModel.networkConnectionState.observe(viewLifecycleOwner) {
            doOnData {
                if (it) {
                    loadWebView()
                    binding.progress.visibility = View.GONE
                } else {
                    binding.root.errorSnack(R.string.human_verification_captcha_no_connectivity)
                }
            }
        }

        viewModel.codeVerificationResult.observe(viewLifecycleOwner) {
            doOnData {
                verificationDone()
            }
            doOnError {
                binding.progress.visibility = View.GONE
                showErrorCode()
            }
        }

        binding.captchaWebView.apply {
            settings.javaScriptEnabled = true // this is fine, required to load captcha
            addJavascriptInterface(WebAppInterface(), "AndroidInterface")
            webChromeClient = CaptchaWebChromeClient()
        }
    }

    private fun loadWebView() {
        binding.run {
            captchaWebView.loadUrl(
                "https://secure.protonmail.com/captcha/captcha.html?token=${humanVerificationBase.urlToken}" +
                    "&client=android&host=$host"
            )
        }
    }

    private fun verificationDone() {
        parentFragmentManager.setFragmentResult(
            HumanVerificationDialogFragment.KEY_VERIFICATION_DONE,
            bundleOf()
        )
    }

    private fun showErrorCode() {
        requireView().errorSnack(R.string.human_verification_captcha_failed)
    }

    inner class CaptchaWebChromeClient : WebChromeClient() {
        override fun onProgressChanged(view: WebView, newProgress: Int) {
            if (isAdded) {
                if (newProgress == MAX_PROGRESS && isAdded) {
                    binding.progress.visibility = View.GONE
                } else {
                    binding.progress.visibility = View.VISIBLE
                }
            }
        }
    }

    inner class WebAppInterface {
        @JavascriptInterface
        fun receiveResponse(message: String) {
            humanVerificationBase.verificationToken = message
            binding.progress.visibility = View.VISIBLE
            viewModel.verifyTokenCode(sessionId, message)
        }
    }
}
