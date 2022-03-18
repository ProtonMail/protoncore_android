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

package me.proton.core.payment.presentation.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.domain.entity.UserId
import me.proton.core.payment.domain.entity.PaymentTokenStatus
import me.proton.core.payment.presentation.R
import me.proton.core.payment.presentation.databinding.ActivityPaymentTokenApprovalBinding
import me.proton.core.payment.presentation.entity.PaymentTokenApprovalInput
import me.proton.core.payment.presentation.entity.PaymentTokenApprovalResult
import me.proton.core.payment.presentation.viewmodel.PaymentTokenApprovalViewModel
import me.proton.core.presentation.utils.errorSnack
import me.proton.core.presentation.utils.getUserMessage
import me.proton.core.presentation.utils.onClick
import me.proton.core.util.kotlin.exhaustive

/**
 * Activity that shows a 3D Secure approval web page for 3DS supported Credit Cards.
 * Note: It has to use JavaScript.
 */
@SuppressLint("SetJavaScriptEnabled")
@AndroidEntryPoint
class PaymentTokenApprovalActivity :
    PaymentsActivity<ActivityPaymentTokenApprovalBinding>(ActivityPaymentTokenApprovalBinding::inflate) {

    private val viewModel by viewModels<PaymentTokenApprovalViewModel>()

    private val input: PaymentTokenApprovalInput by lazy {
        requireNotNull(intent?.extras?.getParcelable(ARG_INPUT))
    }

    private val webView by lazy {
        binding.tokenApprovalWebView.apply {
            settings.javaScriptEnabled = true // suppressed because JavaScript support is needed for PayPal payments
            webChromeClient = LoadingWebChromeClient(MAX_PROGRESS)

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    return request?.let { webRequest ->
                        viewModel.handleRedirection(
                            input.userId?.let { UserId(it) },
                            input.paymentToken,
                            webRequest.url,
                            input.returnHost
                        )
                    } ?: run {
                        false
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.closeButton.onClick {
            onError()
        }
        viewModel.watchNetwork()
        viewModel.networkConnectionState
            .flowWithLifecycle(lifecycle)
            .distinctUntilChanged()
            .onEach {
                it?.let { networkState ->
                    if (networkState) {
                        webView.loadUrl(input.approvalUrl)
                        binding.progress.visibility = View.GONE
                    } else {
                        binding.root.errorSnack(R.string.payments_no_connectivity)
                    }
                }
            }.launchIn(lifecycleScope)

        viewModel.approvalState
            .flowWithLifecycle(lifecycle)
            .distinctUntilChanged()
            .onEach {
                when (it) {
                    is PaymentTokenApprovalViewModel.State.Idle -> showLoading(false)
                    is PaymentTokenApprovalViewModel.State.Processing -> showLoading(true)
                    is PaymentTokenApprovalViewModel.State.Success -> onSuccess(it.paymentTokenStatus)
                    is PaymentTokenApprovalViewModel.State.Error ->
                        showError(it.error.getUserMessage(resources))
                }.exhaustive
            }.launchIn(lifecycleScope)
    }

    override fun onError(message: String?) {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    private fun onSuccess(paymentTokenStatus: PaymentTokenStatus) {
        val intent = Intent().putExtra(
            ARG_RESULT,
            PaymentTokenApprovalResult(
                paymentTokenStatus == PaymentTokenStatus.CHARGEABLE,
                input.amount,
                input.paymentToken
            )
        )
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    inner class LoadingWebChromeClient(
        private val maxProgress: Int
    ) : WebChromeClient() {
        override fun onProgressChanged(view: WebView, newProgress: Int) {
            binding.progress.visibility = if (newProgress == maxProgress)
                View.GONE
            else
                View.VISIBLE
        }
    }

    companion object {
        const val ARG_INPUT = "arg.paymentTokenApprovalInput"
        const val ARG_RESULT = "arg.paymentTokenApprovalResult"
        const val MAX_PROGRESS = 100
    }
}
