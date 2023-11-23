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

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.payment.domain.entity.Currency
import me.proton.core.payment.domain.entity.ProtonPaymentToken
import me.proton.core.payment.domain.entity.SubscriptionCycle
import me.proton.core.payment.domain.usecase.PaymentProvider
import me.proton.core.payment.presentation.LogTag
import me.proton.core.payment.presentation.R
import me.proton.core.payment.presentation.databinding.ActivityBillingBinding
import me.proton.core.payment.presentation.entity.BillingInput
import me.proton.core.payment.presentation.entity.BillingResult
import me.proton.core.payment.presentation.viewmodel.BillingCommonViewModel
import me.proton.core.payment.presentation.viewmodel.BillingViewModel
import me.proton.core.plan.domain.entity.SubscriptionManagement
import me.proton.core.presentation.utils.errorSnack
import me.proton.core.presentation.utils.getUserMessage
import me.proton.core.presentation.utils.onClick
import me.proton.core.util.kotlin.CoreLogger
import me.proton.core.util.kotlin.exhaustive

/**
 * Activity responsible for taking a Credit Card input from a user for the purpose of paying a subscription.
 * It processes the payment request as well.
 * Note, that this one only works with a new Credit Card and is not responsible for displaying existing payment methods.
 */
@AndroidEntryPoint
internal class BillingActivity : PaymentsActivity<ActivityBillingBinding>(ActivityBillingBinding::inflate) {

    private val viewModel by viewModels<BillingViewModel>()

    private val input: BillingInput by lazy {
        requireNotNull(intent?.extras?.getParcelable(ARG_BILLING_INPUT))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            findOutPlan()
            toolbar.apply {
                if (input.userId != null) {
                    navigationIcon = ContextCompat.getDrawable(context, R.drawable.ic_proton_arrow_back)
                }
                setNavigationOnClickListener {
                    setResult(RESULT_CANCELED, intent)
                    finish()
                }
            }
            payButton.onClick(::onPayClicked)
            gPayButton.onClick(::onPayClicked)
            nextPaymentProviderButton.onClick(::onNextPaymentProviderClicked)
        }
        observeViewModel()
    }

    @Suppress("IMPLICIT_CAST_TO_ANY")
    private fun observeViewModel() {
        viewModel.plansValidationState
            .flowWithLifecycle(lifecycle)
            .distinctUntilChanged()
            .onEach {
                when (it) {
                    is BillingCommonViewModel.PlansValidationState.Success -> {
                        val amountDue = it.subscription.amountDue
                        val plan = input.plan.copy(amount = amountDue)
                        viewModel.setPlan(plan)
                    }
                    is BillingCommonViewModel.PlansValidationState.Error.Message -> showError(it.message)
                    else -> Unit
                }.exhaustive
            }.launchIn(lifecycleScope)

        viewModel.subscriptionResult
            .flowWithLifecycle(lifecycle)
            .distinctUntilChanged()
            .onEach {
                when (it) {
                    is BillingCommonViewModel.State.Processing -> showLoading(true)
                    is BillingCommonViewModel.State.Success.SignUpTokenReady -> onBillingSuccess(
                        it.paymentToken,
                        it.amount,
                        it.currency,
                        it.cycle,
                        it.subscriptionManagement
                    )
                    is BillingCommonViewModel.State.Success.SubscriptionCreated -> onBillingSuccess(
                        amount = it.amount,
                        currency = it.currency,
                        cycle = it.cycle,
                        subscriptionManagement = it.subscriptionManagement
                    )
                    is BillingCommonViewModel.State.Incomplete.TokenApprovalNeeded ->
                        onTokenApprovalNeeded(input.userId, it.paymentTokenResult, it.amount)
                    is BillingCommonViewModel.State.Error.General -> showError(it.error.getUserMessage(resources))
                    is BillingCommonViewModel.State.Error.SignUpWithPaymentMethodUnsupported ->
                        showError(getString(R.string.payments_error_signup_paymentmethod))
                    else -> {
                        // no operation, not interested in other events
                    }
                }
            }.launchIn(lifecycleScope)

        viewModel.state
            .flowWithLifecycle(lifecycle)
            .distinctUntilChanged()
            .onEach {
                with(binding) {
                    when (it) {
                        is BillingViewModel.State.PaymentProvidersError.Message -> showError(it.error)
                        is BillingViewModel.State.PaymentProvidersSuccess -> {
                            with(binding) {
                                val currentProvider = it.activeProvider
                                when (currentProvider) {
                                    PaymentProvider.GoogleInAppPurchase -> {
                                        supportFragmentManager.showBillingIAPFragment(R.id.fragment_container)
                                        handleNextProviderVisibility(PaymentProvider.GoogleInAppPurchase)
                                    }
                                    PaymentProvider.CardPayment -> {
                                        supportFragmentManager.showBillingFragment(R.id.fragment_container)
                                        handleNextProviderVisibility(PaymentProvider.CardPayment)
                                    }
                                    PaymentProvider.PayPal -> error("PayPal is not supported")
                                }.exhaustive
                                viewModel.announcePlan()
                                it.nextPaymentProviderTextResource?.let { textResource ->
                                    nextPaymentProviderButton.text = getString(textResource)
                                } ?: run {
                                    nextPaymentProviderButton.visibility = View.GONE
                                }
                            }
                        }
                        BillingViewModel.State.PaymentProvidersEmpty -> {
                            val message = getString(R.string.payments_no_payment_provider)
                            CoreLogger.i(LogTag.EMPTY_ACTIVE_PAYMENT_PROVIDER, message)
                            setResult(RESULT_CANCELED, intent)
                            finish()
                        }
                        is BillingViewModel.State.PayButtonsState.ProtonPayDisabled -> {
                            payButton.apply {
                                isEnabled = false
                                visibility = View.VISIBLE
                            }
                            gPayButton.visibility = View.INVISIBLE
                        }
                        is BillingViewModel.State.PayButtonsState.GPayDisabled -> {
                            gPayButton.apply {
                                isEnabled = false
                                visibility = View.VISIBLE
                            }
                            payButton.visibility = View.INVISIBLE
                        }
                        is BillingViewModel.State.PayButtonsState.ProtonPayEnabled -> {
                            payButton.visibility = View.VISIBLE
                            gPayButton.visibility = View.INVISIBLE
                            payButton.apply {
                                isEnabled = true
                                text = it.text
                            }
                        }
                        is BillingViewModel.State.PayButtonsState.GPayEnabled -> {
                            payButton.visibility = View.INVISIBLE
                            gPayButton.visibility = View.VISIBLE
                            gPayButton.isEnabled = true
                        }
                        is BillingViewModel.State.PayButtonsState.Idle -> {
                            payButton.setIdle()
                            gPayButton.apply {
                                setIdle()
                                text = getString(R.string.payments_pay_with)
                                icon = AppCompatResources.getDrawable(this@BillingActivity, R.drawable.ic_gpay_logo)
                            }
                            nextPaymentProviderButton.isEnabled = true
                        }
                        is BillingViewModel.State.PayButtonsState.Loading -> {
                            payButton.setLoading()
                            gPayButton.apply {
                                text = getString(R.string.payments_paying_in_process)
                                icon = null
                                setLoading()
                            }
                            nextPaymentProviderButton.isEnabled = false
                        }
                        is BillingViewModel.State.Idle,
                        is BillingViewModel.State.Loading -> {
                            // do nothing
                        }
                    }.exhaustive
                }
            }.launchIn(lifecycleScope)
    }

    private fun handleNextProviderVisibility(currentProvider: PaymentProvider) {
        binding.nextPaymentProviderButton.visibility =
            if (input.singlePaymentProvider == currentProvider)
                View.GONE
            else
                View.VISIBLE
    }

    private fun onBillingSuccess(
        token: ProtonPaymentToken? = null,
        amount: Long,
        currency: Currency,
        cycle: SubscriptionCycle,
        subscriptionManagement: SubscriptionManagement
    ) {
        val intent = Intent()
            .putExtra(
                ARG_BILLING_RESULT,
                BillingResult(
                    paySuccess = true,
                    token = token?.value,
                    subscriptionCreated = token == null,
                    amount = amount,
                    currency = currency,
                    cycle = cycle,
                    subscriptionManagement = subscriptionManagement
                )
            )
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun findOutPlan() = with(input) {
        if (plan.amount == null) {
            viewModel.validatePlan(user, listOf(plan.name), codes, plan.currency, plan.subscriptionCycle)
        }
        viewModel.setPlan(plan)
    }

    override fun onThreeDSApprovalResult(amount: Long, token: ProtonPaymentToken, success: Boolean) {
        if (!success) {
            binding.payButton.setIdle()
            return
        }
        with(input) {
            val plans = listOf(plan.name)
            viewModel.onThreeDSTokenApproved(
                user,
                plans,
                codes,
                amount,
                plan.currency,
                plan.subscriptionCycle,
                token,
                SubscriptionManagement.PROTON_MANAGED
            )
        }
    }

    private fun onPayClicked() {
        viewModel.onPay(input)
    }

    private fun onNextPaymentProviderClicked() {
        viewModel.switchNextPaymentProvider()
    }

    override fun showLoading(loading: Boolean) = with(binding) {
        if (loading) {
            payButton.setLoading()
            gPayButton.apply {
                setLoading()
                icon = null
                text = getString(R.string.payments_paying_in_process)
            }
        } else {
            payButton.setIdle()
            gPayButton.apply {
                setIdle()
                text = getString(R.string.payments_pay_with)
                icon = AppCompatResources.getDrawable(this@BillingActivity, R.drawable.ic_gpay_logo)
            }
        }
        viewModel.onLoadingStateChange(loading)
    }

    override fun showError(message: String?) {
        showLoading(false)
        binding.root.errorSnack(message = message ?: getString(R.string.payments_general_error))
    }

    companion object {
        const val ARG_BILLING_INPUT = "arg.billingInput"
        const val ARG_BILLING_RESULT = "arg.billingResult"
        const val EXP_DATE_SEPARATOR = "/"
    }
}
