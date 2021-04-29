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
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.auth.presentation.R
import me.proton.core.auth.presentation.databinding.FragmentTermsConditionsBinding
import me.proton.core.auth.presentation.viewmodel.signup.TermsConditionsViewModel
import me.proton.core.presentation.ui.ProtonDialogFragment
import me.proton.core.presentation.utils.errorSnack
import me.proton.core.presentation.utils.onClick

@AndroidEntryPoint
class TermsConditionsDialogFragment : ProtonDialogFragment<FragmentTermsConditionsBinding>() {

    private val viewModel by viewModels<TermsConditionsViewModel>()

    override fun layoutId() = R.layout.fragment_terms_conditions

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.closeButton.onClick {
            dismissAllowingStateLoss()
        }

        viewModel.networkConnectionState.onEach {
            it?.let { networkState ->
                if (networkState) {
                    binding.termsConditionsWebView.apply {
                        webChromeClient = CaptchaWebChromeClient()
                        loadUrl(TERMS_CONDITIONS_URL)
                    }
                } else {
                    binding.root.errorSnack(R.string.auth_signup_no_connectivity)
                }
            }
        }.launchIn(lifecycleScope)
        viewModel.watchNetwork()
    }

    override fun onBackPressed() {
        dismissAllowingStateLoss()
    }

    inner class CaptchaWebChromeClient : WebChromeClient() {
        override fun onProgressChanged(view: WebView, newProgress: Int) {
            if (isAdded) {
                with(binding.progress) {
                    visibility = if (newProgress == MAX_PROGRESS && isAdded) View.GONE else View.VISIBLE
                }
            }
        }
    }

    companion object {
        const val TERMS_CONDITIONS_URL = "https://protonmail.com/ios-terms-and-conditions.html"
        private const val MAX_PROGRESS = 100
    }
}
