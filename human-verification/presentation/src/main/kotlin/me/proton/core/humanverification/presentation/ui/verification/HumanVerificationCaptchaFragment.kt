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
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.humanverification.domain.entity.TokenType
import me.proton.core.humanverification.presentation.CaptchaHost
import me.proton.core.humanverification.presentation.R
import me.proton.core.humanverification.presentation.databinding.FragmentHumanVerificationCaptchaBinding
import me.proton.core.humanverification.presentation.ui.HumanVerificationDialogFragment
import me.proton.core.humanverification.presentation.ui.verification.HumanVerificationMethodCommon.Companion.ARG_URL_TOKEN
import me.proton.core.humanverification.presentation.viewmodel.verification.HumanVerificationCaptchaViewModel
import me.proton.core.presentation.ui.ProtonFragment
import me.proton.core.presentation.utils.errorSnack
import me.proton.core.presentation.viewmodel.ViewModelResult
import me.proton.core.util.kotlin.exhaustive
import javax.inject.Inject

/**
 * Fragment that handles human verification with Captcha.
 */
@AndroidEntryPoint
internal class HumanVerificationCaptchaFragment : ProtonFragment<FragmentHumanVerificationCaptchaBinding>() {

    @Inject
    @CaptchaHost
    lateinit var captchaHost: String

    private val viewModel by viewModels<HumanVerificationCaptchaViewModel>()

    private val captchaUrl: String by lazy {
        requireArguments().get(ARG_CAPTCHA_URL) as String? ?: "https://$captchaHost/api/core/v4/captcha"
    }

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

        binding.captchaWebView.apply {
            settings.javaScriptEnabled = true // this is fine, required to load captcha
            addJavascriptInterface(WebAppInterface(), "AndroidInterface")
            webChromeClient = CaptchaWebChromeClient()
        }

        viewModel.networkConnectionState.onEach {
            when (it) {
                is ViewModelResult.None,
                is ViewModelResult.Processing -> Unit
                is ViewModelResult.Error -> {
                    binding.root.errorSnack(R.string.human_verification_captcha_no_connectivity)
                }
                is ViewModelResult.Success -> {
                    loadWebView()
                    binding.progress.visibility = View.GONE
                }
            }.exhaustive
        }.launchIn(lifecycleScope)
    }

    private fun loadWebView() {
        binding.run {
            captchaWebView.loadUrl("$captchaUrl?Token=${humanVerificationBase.urlToken}")
        }
    }

    private fun verificationDone(token: String) {
        parentFragmentManager.setFragmentResult(
            HumanVerificationDialogFragment.KEY_VERIFICATION_DONE,
            bundleOf(
                HumanVerificationDialogFragment.ARG_TOKEN_CODE to token,
                HumanVerificationDialogFragment.ARG_TOKEN_TYPE to TokenType.CAPTCHA.value
            )
        )
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
            verificationDone(message)
        }
    }

    companion object {
        private const val ARG_CAPTCHA_URL = "arg.captchaUrl"
        private const val MAX_PROGRESS = 100

        operator fun invoke(
            captchaUrl: String? = null,
            urlToken: String
        ) = HumanVerificationCaptchaFragment().apply {
            arguments = bundleOf(
                ARG_CAPTCHA_URL to captchaUrl,
                ARG_URL_TOKEN to urlToken
            )
        }
    }
}
