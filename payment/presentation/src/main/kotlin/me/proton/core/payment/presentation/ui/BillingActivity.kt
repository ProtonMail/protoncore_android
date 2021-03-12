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

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import me.proton.core.payment.domain.entity.Card
import me.proton.core.payment.domain.entity.PaymentType
import me.proton.core.payment.presentation.R
import me.proton.core.payment.presentation.databinding.ActivityBillingBinding
import me.proton.core.payment.presentation.entity.BillingInput
import me.proton.core.payment.presentation.entity.BillingResult
import me.proton.core.payment.presentation.viewmodel.BillingViewModel
import me.proton.core.presentation.utils.ProtonInputEndButtonAction
import me.proton.core.presentation.utils.hideKeyboard
import me.proton.core.presentation.utils.onClick
import me.proton.core.util.kotlin.exhaustive

/**
 * Activity responsible for taking a Credit Card input from a user for the purpose of paying a subscription.
 * It processes the payment request as well.
 * Note, that this one only works with a new Credit Card and is not responsible for displaying existing payment methods.
 */
@AndroidEntryPoint
class BillingActivity : PaymentsActivity<ActivityBillingBinding>() {

    private val viewModel by viewModels<BillingViewModel>()

    override fun layoutId(): Int = R.layout.activity_billing

    private val input: BillingInput by lazy {
        requireNotNull(intent?.extras?.getParcelable(ARG_BILLING_INPUT))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            findOutPlan()
            closeButton.onClick {
                onBackPressed()
            }
            payButton.onClick(::onPayClicked)

            cardNumberInput.apply {
                setActionMode(
                    ProtonInputEndButtonAction.CUSTOM_ICON,
                    ContextCompat.getDrawable(context, R.drawable.ic_credit_card)
                )
                setTextWatcher(afterTextChangeListener = CardNumberWatcher().watcher)
            }

            expirationDateInput.apply {
                setTextWatcher(afterTextChangeListener = ExpirationDateWatcher().watcher)
            }
        }
        observe()
    }

    private fun observe() {
        viewModel.countriesResult.observeData { countries ->
            binding.countriesText.setAdapter(
                ArrayAdapter(
                    this,
                    android.R.layout.simple_list_item_1,
                    countries.map { it.name })
            )
        }

        viewModel.plansValidationState.observeData {
            when (it) {
                is BillingViewModel.PlansValidationState.Processing -> {
                }
                is BillingViewModel.PlansValidationState.Success -> {
                    binding.selectedPlanDetailsLayout.plan = input.plan.copy(amount = it.subscription.amountDue)
                }
                is BillingViewModel.PlansValidationState.Error.Message -> showError(it.message)
            }.exhaustive
        }

        viewModel.subscriptionResult.observeData {
            when (it) {
                is BillingViewModel.State.Processing -> showLoading(true)
                is BillingViewModel.State.Success.SignUpTokenReady -> onBillingSuccess(it.paymentToken)
                is BillingViewModel.State.Success.SubscriptionCreated -> onBillingSuccess()
                is BillingViewModel.State.Incomplete.TokenApprovalNeeded ->
                    onTokenApprovalNeeded(input.sessionId, it.paymentToken, it.amount)
                is BillingViewModel.State.Error.Message -> showError(it.message)
                is BillingViewModel.State.Error.SignUpWithPaymentMethodUnsupported ->
                    showError(getString(R.string.payments_error_signup_paymentmethod))
                else -> {
                    // no operation, not interested in other events
                }
            }
        }
    }

    private fun onBillingSuccess(token: String? = null) {
        val intent = Intent()
            .putExtra(ARG_RESULT, BillingResult(true, token, token == null))
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun findOutPlan() = with(input) {
        if (plan.amount == null) {
            viewModel.validatePlan(session, listOf(plan.id), codes, plan.currency, plan.subscriptionCycle)
        }
        binding.selectedPlanDetailsLayout.plan = plan
    }

    override fun onThreeDSApproved(amount: Long, token: String) {
        with(input) {
            val plans = listOf(plan.id)
            viewModel.onThreeDSTokenApproved(
                session, plans, codes, amount, plan.currency, plan.subscriptionCycle, token
            )
        }
    }

    private fun onPayClicked() = with(binding) {
        hideKeyboard()
        if (invalidInputItems(this@BillingActivity) > 0) {
            return@with
        }

        val expirationDate = expirationDateInput.text.toString().split(EXP_DATE_SEPARATOR)

        viewModel.subscribe(
            input.session,
            input.existingPlanIds.plus(input.plan.id),
            input.codes,
            input.plan.currency,
            input.plan.subscriptionCycle,
            PaymentType.CreditCard(
                Card.CardWithPaymentDetails(
                    number = cardNumberInput.text.toString(),
                    cvc = cvcInput.text.toString(),
                    expirationMonth = expirationDate[0],
                    expirationYear = expirationDate[1],
                    name = cardNameInput.text.toString(),
                    country = countriesText.text.toString(),
                    zip = postalCodeInput.text.toString()
                )
            )
        )
    }

    override fun showLoading(loading: Boolean) = with(binding) {
        if (loading) {
            payButton.setLoading()
        } else {
            payButton.setIdle()
        }
    }

    companion object {
        const val ARG_BILLING_INPUT = "arg.billingInput"
        const val ARG_BILLING_RESULT = "arg.billingResult"
        const val EXP_DATE_SEPARATOR = "/"
    }
}
