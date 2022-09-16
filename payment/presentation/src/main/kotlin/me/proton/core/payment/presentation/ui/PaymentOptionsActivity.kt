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
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.domain.entity.UserId
import me.proton.core.payment.domain.entity.PaymentMethodType
import me.proton.core.payment.domain.entity.PaymentType
import me.proton.core.payment.domain.entity.SubscriptionManagement
import me.proton.core.payment.domain.usecase.PaymentProvider
import me.proton.core.payment.presentation.R
import me.proton.core.payment.presentation.databinding.ActivityPaymentOptionsBinding
import me.proton.core.payment.presentation.databinding.ItemPaymentMethodBinding
import me.proton.core.payment.presentation.entity.BillingResult
import me.proton.core.payment.presentation.entity.CurrentSubscribedPlanDetails
import me.proton.core.payment.presentation.entity.PaymentOptionUIModel
import me.proton.core.payment.presentation.entity.PaymentOptionsInput
import me.proton.core.payment.presentation.viewmodel.BillingCommonViewModel
import me.proton.core.payment.presentation.viewmodel.PaymentOptionsViewModel
import me.proton.core.presentation.ui.adapter.selectableProtonAdapter
import me.proton.core.presentation.utils.getUserMessage
import me.proton.core.presentation.utils.onClick
import me.proton.core.util.kotlin.exhaustive

@AndroidEntryPoint
internal class PaymentOptionsActivity :
    PaymentsActivity<ActivityPaymentOptionsBinding>(ActivityPaymentOptionsBinding::inflate) {

    private val viewModel by viewModels<PaymentOptionsViewModel>()

    private val input: PaymentOptionsInput by lazy {
        requireNotNull(intent?.extras?.getParcelable(ARG_INPUT))
    }

    private val user by lazy {
        UserId(input.userId)
    }
    private var selectedPaymentMethodId: String? = null

    private var amountDue: Long? = null

    private val paymentOptionsAdapter = selectableProtonAdapter(
        getView = { parent, inflater -> ItemPaymentMethodBinding.inflate(inflater, parent, false) },
        onBind = { paymentMethod, selected, position ->
            when (paymentMethod) {
                is PaymentOptionUIModel.InAppPurchase -> {
                    paymentMethodTitleText.text = paymentMethod.provider
                    paymentMethodSubtitleText.visibility = View.GONE
                    val drawable = ContextCompat.getDrawable(this@PaymentOptionsActivity, R.drawable.ic_gpay)
                    paymentMethodIcon.setImageDrawable(drawable)
                    paymentMethodRadio.isChecked = selected
                }
                is PaymentOptionUIModel.PaymentMethod -> {
                    paymentMethodTitleText.text = paymentMethod.title
                    paymentMethodSubtitleText.apply {
                        text = paymentMethod.subtitle
                        visibility = View.VISIBLE
                    }

                    val drawableRes = when (PaymentMethodType.map[paymentMethod.type]) {
                        PaymentMethodType.CARD -> R.drawable.ic_proton_credit_card
                        PaymentMethodType.PAYPAL -> R.drawable.ic_paypal
                        null -> R.drawable.ic_proton_credit_card
                    }
                    val drawable = ContextCompat.getDrawable(this@PaymentOptionsActivity, drawableRes)
                    paymentMethodIcon.setImageDrawable(drawable)
                    paymentMethodRadio.isChecked = selected
                    if (position == 0 && selectedPaymentMethodId == null) {
                        paymentMethodRadio.isChecked = true
                        selectedPaymentMethodId = paymentMethod.id
                    } else {
                        // do nothing
                    }
                }
            }.exhaustive
        },
        onItemClick = ::onPaymentMethodClicked,
        diffCallback = PaymentOptionUIModel.DiffCallback
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            toolbar.setNavigationOnClickListener {
                onBackPressed()
            }
            paymentMethodsList.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = paymentOptionsAdapter
            }
            addCreditCardButton.onClick { startBilling() }
            selectedPlanDetailsLayout.plan = input.plan
            payButton.apply {
                isEnabled = false
                text = String.format(getString(R.string.payments_pay), selectedPlanDetailsLayout.userReadablePlanAmount)
            }
        }
        observe()

        viewModel.getAvailablePaymentMethods(user)
    }

    private fun observe() {
        viewModel.availablePaymentMethodsState
            .flowWithLifecycle(lifecycle)
            .distinctUntilChanged()
            .onEach {
                when (it) {
                    is PaymentOptionsViewModel.State.Success.PaymentMethodsSuccess ->
                        onSuccess(it.availablePaymentMethods)
                    is PaymentOptionsViewModel.State.Error.General -> showError(it.error.getUserMessage(resources))
                    else -> {
                        // do nothing
                    }
                }.exhaustive
            }.launchIn(lifecycleScope)

        viewModel.plansValidationState
            .flowWithLifecycle(lifecycle)
            .distinctUntilChanged()
            .onEach {
                when (it) {
                    is BillingCommonViewModel.PlansValidationState.Success -> {
                        amountDue = it.subscription.amountDue
                        with(binding) {
                            selectedPlanDetailsLayout.plan = input.plan.copy(amount = it.subscription.amountDue)
                            payButton.text = String.format(
                                getString(R.string.payments_pay),
                                selectedPlanDetailsLayout.userReadablePlanAmount
                            )
                        }
                    }
                    is BillingCommonViewModel.PlansValidationState.Error.Message -> showError(it.message)
                    else -> {
                    }
                }.exhaustive
            }.launchIn(lifecycleScope)

        viewModel.subscriptionResult
            .flowWithLifecycle(lifecycle)
            .distinctUntilChanged()
            .onEach {
                when (it) {
                    is BillingCommonViewModel.State.Processing -> showLoading(true)
                    is BillingCommonViewModel.State.Success.SubscriptionCreated -> onPaymentResult(
                        BillingResult(
                            paySuccess = true,
                            token = it.paymentToken,
                            subscriptionCreated = true,
                            amount = it.amount,
                            currency = it.currency,
                            cycle = it.cycle,
                            subscriptionManagement = it.subscriptionManagement
                        )
                    )
                    is BillingCommonViewModel.State.Incomplete.TokenApprovalNeeded ->
                        onTokenApprovalNeeded(input.userId, it.paymentToken, it.amount)
                    is BillingCommonViewModel.State.Error.General -> showError(it.error.getUserMessage(resources))
                    is BillingCommonViewModel.State.Error.SignUpWithPaymentMethodUnsupported ->
                        showError(getString(R.string.payments_error_signup_paymentmethod))
                    else -> {
                    }
                }.exhaustive
            }.launchIn(lifecycleScope)
    }

    private fun onPayCreditCard() {
        viewModel.subscribe(
            user,
            input.plan.name,
            input.plan.services,
            input.plan.type,
            input.codes,
            input.plan.currency,
            input.plan.subscriptionCycle,
            PaymentType.PaymentMethod(selectedPaymentMethodId!!),
            SubscriptionManagement.PROTON_MANAGED
        )
    }

    private fun startBilling(singlePaymentProvider: PaymentProvider? = null) {
        startBilling(
            input.userId,
            viewModel.currentPlans.map {
                CurrentSubscribedPlanDetails(
                    name = it.name,
                    services = it.services,
                    type = it.type
                )
            },
            input.plan.copy(amount = amountDue),
            input.codes,
            singlePaymentProvider
        )
    }

    private fun onPaymentMethodClicked(paymentMethod: PaymentOptionUIModel) {
        val googleProviderId = getString(R.string.payments_method_google).lowercase()
        with(binding) {
            if (paymentMethod.id == googleProviderId) {
                paymentOptionsIapTerms.visibility = View.VISIBLE
                payButton.text = getString(R.string.payments_method_continue)
                payButton.onClick(PaymentProvider.GoogleInAppPurchase, ::startBilling)
            } else {
                paymentOptionsIapTerms.visibility = View.INVISIBLE
                payButton.text =
                    String.format(getString(R.string.payments_pay), selectedPlanDetailsLayout.userReadablePlanAmount)
                payButton.onClick(::onPayCreditCard)
            }
        }
        selectedPaymentMethodId = paymentMethod.id
        paymentOptionsAdapter.notifyItemChanged(0) // invalidate the first option
    }

    override fun onThreeDSApprovalResult(amount: Long, token: String, success: Boolean) {
        if (!success) {
            binding.payButton.setIdle()
            return
        }
        viewModel.onThreeDSTokenApproved(
            user,
            input.plan.name,
            input.plan.services,
            input.plan.type,
            input.codes,
            amount,
            input.plan.currency,
            input.plan.subscriptionCycle,
            token,
            SubscriptionManagement.PROTON_MANAGED
        )
    }

    private fun onSuccess(availablePaymentMethods: List<PaymentOptionUIModel>) {
        if (availablePaymentMethods.isEmpty() || (availablePaymentMethods.size == 1 && availablePaymentMethods[0] is PaymentOptionUIModel.InAppPurchase)) {
            startBilling()
            return
        }
        viewModel.validatePlan(
            user,
            input.plan.name,
            input.plan.services,
            input.plan.type,
            input.codes,
            input.plan.currency,
            input.plan.subscriptionCycle
        )
        paymentOptionsAdapter.submitList(availablePaymentMethods)
        binding.apply {
            payButton.isEnabled = true
            progressLayout.visibility = View.GONE
        }
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

        inline fun View.onClick(value: PaymentProvider, crossinline block: (PaymentProvider) -> Unit) {
            setOnClickListener { block(value) }
        }
    }
}
