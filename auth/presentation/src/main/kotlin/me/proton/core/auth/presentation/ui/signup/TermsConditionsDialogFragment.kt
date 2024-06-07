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

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.auth.presentation.R
import me.proton.core.auth.presentation.databinding.FragmentTermsConditionsBinding
import me.proton.core.auth.presentation.viewmodel.signup.TermsConditionsViewModel
import me.proton.core.network.domain.NetworkPrefs
import me.proton.core.network.domain.client.ExtraHeaderProvider
import me.proton.core.presentation.ui.ProtonDialogFragment
import me.proton.core.presentation.ui.webview.ProtonWebViewClient
import me.proton.core.presentation.utils.errorSnack
import me.proton.core.presentation.utils.viewBinding
import javax.inject.Inject

@AndroidEntryPoint
class TermsConditionsDialogFragment : ProtonDialogFragment(R.layout.fragment_terms_conditions) {

    @Inject
    internal lateinit var customWebViewClient: CustomWebViewClient

    private val viewModel by viewModels<TermsConditionsViewModel>()
    private val binding by viewBinding(FragmentTermsConditionsBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { dismissAllowingStateLoss() }
        binding.termsConditionsWebView.setAllowForceDark()
        customWebViewClient.progress = binding.progress

        viewModel.networkState
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { connected ->
                if (connected) {
                    binding.termsConditionsWebView.apply {
                        webViewClient = customWebViewClient
                        loadUrl(TERMS_CONDITIONS_URL)
                    }
                } else {
                    binding.root.errorSnack(R.string.auth_signup_no_connectivity)
                }
            }
            .launchIn(lifecycleScope)
    }

    override fun onDestroyView() {
        customWebViewClient.progress = null
        super.onDestroyView()
    }

    override fun onBackPressed() {
        dismissAllowingStateLoss()
    }

    internal class CustomWebViewClient @Inject constructor(
        extraHeaderProvider: ExtraHeaderProvider,
        networkPrefs: NetworkPrefs
    ) : ProtonWebViewClient(networkPrefs, extraHeaderProvider) {
        var progress: View? = null

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            progress?.visibility = View.VISIBLE
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            progress?.visibility = View.GONE
        }
    }

    companion object {
        const val TERMS_CONDITIONS_URL = "https://proton.me/legal/terms-ios"
    }
}
