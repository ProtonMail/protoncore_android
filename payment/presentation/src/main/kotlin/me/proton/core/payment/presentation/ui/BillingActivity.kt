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
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.country.presentation.entity.CountryUIModel
import me.proton.core.country.presentation.ui.CountryPickerFragment
import me.proton.core.country.presentation.ui.showCountryPicker
import me.proton.core.payment.domain.entity.Card
import me.proton.core.payment.domain.entity.PaymentType
import me.proton.core.payment.presentation.R
import me.proton.core.payment.presentation.databinding.ActivityBillingBinding
import me.proton.core.payment.presentation.entity.BillingInput
import me.proton.core.payment.presentation.entity.BillingResult
import me.proton.core.payment.presentation.viewmodel.BillingViewModel
import me.proton.core.presentation.ui.view.ProtonInput
import me.proton.core.presentation.utils.hideKeyboard
import me.proton.core.presentation.utils.onClick
import me.proton.core.presentation.utils.onTextChange
import me.proton.core.util.kotlin.exhaustive

/**
 * Activity responsible for taking a Credit Card input from a user for the purpose of paying a subscription.
 * It processes the payment request as well.
 * Note, that this one only works with a new Credit Card and is not responsible for displaying existing payment methods.
 */
@AndroidEntryPoint
class BillingActivity : PaymentsActivity<ActivityBillingBinding>() {

    private val viewModel by viewModels<BillingViewModel>()

    private val input: BillingInput by lazy {
        requireNotNull(intent?.extras?.getParcelable(ARG_BILLING_INPUT))
    }

    override fun layoutId(): Int = R.layout.activity_billing

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            findOutPlan()
            closeButton.apply {
                if (input.userId != null) {
                    setIconResource(R.drawable.ic_arrow_back)
                }
                onClick {
                    onBackPressed()
                }
            }
            payButton.onClick(::onPayClicked)

            cardNumberInput.apply {
                endIconMode = ProtonInput.EndIconMode.CUSTOM_ICON
                endIconDrawable = ContextCompat.getDrawable(context, R.drawable.ic_credit_card)
                onTextChange(afterTextChangeListener = CardNumberWatcher().watcher)
            }

            expirationDateInput.apply {
                onTextChange(afterTextChangeListener = ExpirationDateWatcher().watcher)
            }

            countriesText.onClick {
                supportFragmentManager.showCountryPicker(false)
            }
            supportFragmentManager.setFragmentResultListener(
                CountryPickerFragment.KEY_COUNTRY_SELECTED, this@BillingActivity
            ) { _, bundle ->
                val country = bundle.getParcelable<CountryUIModel>(CountryPickerFragment.BUNDLE_KEY_COUNTRY)
                countriesText.text = country?.name
            }
        }
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.plansValidationState.onEach {
            when (it) {
                is BillingViewModel.PlansValidationState.Success -> {
                    binding.selectedPlanDetailsLayout.plan = input.plan.copy(amount = it.subscription.amountDue)
                }
                is BillingViewModel.PlansValidationState.Error.Message -> showError(it.message)
                else -> {
                }
            }.exhaustive
        }.launchIn(lifecycleScope)

        viewModel.subscriptionResult.onEach {
            when (it) {
                is BillingViewModel.State.Processing -> showLoading(true)
                is BillingViewModel.State.Success.SignUpTokenReady -> onBillingSuccess(it.paymentToken)
                is BillingViewModel.State.Success.SubscriptionCreated -> onBillingSuccess()
                is BillingViewModel.State.Incomplete.TokenApprovalNeeded ->
                    onTokenApprovalNeeded(input.userId, it.paymentToken, it.amount)
                is BillingViewModel.State.Error.Message -> showError(it.message)
                is BillingViewModel.State.Error.SignUpWithPaymentMethodUnsupported ->
                    showError(getString(R.string.payments_error_signup_paymentmethod))
                else -> {
                    // no operation, not interested in other events
                }
            }
        }.launchIn(lifecycleScope)
    }

    private fun onBillingSuccess(token: String? = null) {
        val intent = Intent()
            .putExtra(ARG_RESULT, BillingResult(true, token, token == null))
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun findOutPlan() = with(input) {
        if (plan.amount == null) {
            viewModel.validatePlan(user, listOf(plan.id), codes, plan.currency, plan.subscriptionCycle)
        }
        binding.selectedPlanDetailsLayout.plan = plan
    }

    override fun onThreeDSApprovalResult(amount: Long, token: String, success: Boolean) {
        if (!success) {
            binding.payButton.setIdle()
            return
        }
        with(input) {
            val plans = listOf(plan.id)
            viewModel.onThreeDSTokenApproved(
                user, plans, codes, amount, plan.currency, plan.subscriptionCycle, token
            )
        }
    }

    private fun onPayClicked() = with(binding) {
        hideKeyboard()
        val numberOfInvalidFields = billingInputFieldsValidationList(this@BillingActivity).filter {
            !it.isValid
        }.size

        if (numberOfInvalidFields > 0) {
            return@with
        }

        val expirationDate = expirationDateInput.text.toString().split(EXP_DATE_SEPARATOR)

        viewModel.subscribe(
            input.user,
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
        inputState(enabled = !loading)
    }

    private fun inputState(enabled: Boolean) {
        with(binding) {
            cardNameInput.isEnabled = enabled
            cardNumberInput.isEnabled = enabled
            expirationDateInput.isEnabled = enabled
            cvcInput.isEnabled = enabled
            postalCodeInput.isEnabled = enabled
            countriesText.isEnabled = enabled
        }
    }

    companion object {
        const val ARG_BILLING_INPUT = "arg.billingInput"
        const val ARG_BILLING_RESULT = "arg.billingResult"
        const val EXP_DATE_SEPARATOR = "/"
    }
}
