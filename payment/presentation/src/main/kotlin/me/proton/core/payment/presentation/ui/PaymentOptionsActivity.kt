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

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import me.proton.core.network.domain.session.SessionId
import me.proton.core.payment.domain.entity.PaymentMethodType
import me.proton.core.payment.domain.entity.PaymentType
import me.proton.core.payment.presentation.R
import me.proton.core.payment.presentation.databinding.ActivityPaymentOptionsBinding
import me.proton.core.payment.presentation.databinding.ItemPaymentMethodBinding
import me.proton.core.payment.presentation.entity.BillingResult
import me.proton.core.payment.presentation.entity.PaymentOptionUIModel
import me.proton.core.payment.presentation.entity.PaymentOptionsInput
import me.proton.core.payment.presentation.viewmodel.BillingViewModel
import me.proton.core.payment.presentation.viewmodel.PaymentOptionsViewModel
import me.proton.core.presentation.ui.adapter.selectableProtonAdapter
import me.proton.core.presentation.utils.onClick
import me.proton.core.util.kotlin.exhaustive

@AndroidEntryPoint
class PaymentOptionsActivity : PaymentsActivity<ActivityPaymentOptionsBinding>() {

    private val viewModel by viewModels<PaymentOptionsViewModel>()

    override fun layoutId(): Int = R.layout.activity_payment_options

    private val input: PaymentOptionsInput by lazy {
        requireNotNull(intent?.extras?.getParcelable(ARG_INPUT))
    }
    private val session by lazy {
        SessionId(input.sessionId)
    }

    private lateinit var selectedPaymentMethodId: String
    private var amountDue: Long? = null

    private val paymentOptionsAdapter = selectableProtonAdapter(
        getView = { parent, inflater -> ItemPaymentMethodBinding.inflate(inflater, parent, false) },
        onBind = { paymentMethod, selected ->
            paymentMethodTitleText.text = paymentMethod.title
            paymentMethodSubtitleText.text = paymentMethod.subtitle
            val paymentOptionType = PaymentMethodType.values()[paymentMethod.type]
            val drawable = when (paymentOptionType) {
                PaymentMethodType.CARD -> ContextCompat.getDrawable(
                    this@PaymentOptionsActivity,
                    R.drawable.ic_credit_card
                )
                PaymentMethodType.PAYPAL -> ContextCompat.getDrawable(this@PaymentOptionsActivity, R.drawable.ic_paypal)
            }.exhaustive
            paymentMethodIcon.setImageDrawable(drawable)
            paymentMethodRadio.isChecked = selected
        },
        onItemClick = ::onPaymentMethodClicked,
        diffCallback = PaymentOptionUIModel.DiffCallback
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            closeButton.onClick {
                onBackPressed()
            }
            paymentMethodsList.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = paymentOptionsAdapter
            }
            addCreditCardButton.onClick {
                with(input) {
                    startBilling(sessionId, viewModel.currentPlans, plan.copy(amount = amountDue), codes)
                }
            }
            selectedPlanDetailsLayout.plan = input.plan
            payButton.onClick {
                viewModel.subscribe(
                    session,
                    input.plan.id,
                    input.codes,
                    input.plan.currency,
                    input.plan.subscriptionCycle,
                    PaymentType.PaymentMethod(selectedPaymentMethodId)
                )
            }
        }
        observe()

        viewModel.getAvailablePaymentMethods(session)
    }

    private fun observe() {
        viewModel.availablePaymentMethodsState.observeData {
            when (it) {
                is PaymentOptionsViewModel.State.Success.PaymentMethodsSuccess -> onSuccess(it.availablePaymentMethods)
                is PaymentOptionsViewModel.State.Error.Message -> showError(it.message)
                is PaymentOptionsViewModel.State.Error.InvalidSession ->
                    showError(getString(R.string.payments_error_invalid_session))
                else -> {
                }
            }
        }

        viewModel.plansValidationState.observeData {
            when (it) {
                is BillingViewModel.PlansValidationState.Processing -> {
                }
                is BillingViewModel.PlansValidationState.Success -> {
                    amountDue = it.subscription.amountDue
                    binding.selectedPlanDetailsLayout.plan = input.plan.copy(amount = it.subscription.amountDue)
                }
                is BillingViewModel.PlansValidationState.Error.Message -> showError(it.message)
            }.exhaustive
        }

        viewModel.subscriptionResult.observeData {
            when (it) {
                is BillingViewModel.State.Processing -> showLoading(true)
                is BillingViewModel.State.Success.SubscriptionCreated -> onPaymentSuccess(
                    BillingResult(
                        true,
                        it.paymentToken,
                        true
                    )
                )
                is BillingViewModel.State.Incomplete.TokenApprovalNeeded ->
                    onTokenApprovalNeeded(input.sessionId, it.paymentToken, it.amount)
                is BillingViewModel.State.Error.Message -> showError(it.message)
                is BillingViewModel.State.Error.SignUpWithPaymentMethodUnsupported ->
                    showError(getString(R.string.payments_error_signup_paymentmethod))
                else -> {
                }
            }.exhaustive
        }
    }

    private fun onPaymentMethodClicked(paymentMethod: PaymentOptionUIModel) {
        selectedPaymentMethodId = paymentMethod.id
    }

    override fun onThreeDSApproved(amount: Long, token: String) {
        with(input) {
            viewModel.onThreeDSTokenApproved(
                session, plan.id, codes, amount, plan.currency, plan.subscriptionCycle, token
            )
        }
    }

    private fun onSuccess(availablePaymentMethods: List<PaymentOptionUIModel>) {
        if (availablePaymentMethods.isEmpty()) {
            with(input) {
                startBilling(sessionId, viewModel.currentPlans, plan.copy(amount = amountDue), codes)
            }
            return
        }
        with(input) {
            viewModel.validatePlan(session, plan.id, codes, plan.currency, plan.subscriptionCycle)
        }
        binding.progressLayout.visibility = View.GONE
        paymentOptionsAdapter.submitList(availablePaymentMethods)
    }

    override fun showLoading(loading: Boolean) = with(binding) {
        if (loading) {
            payButton.setLoading()
        } else {
            payButton.setIdle()
        }
    }

    companion object {
        const val ARG_INPUT = "arg.paymentsOptionsInput"
    }
}
