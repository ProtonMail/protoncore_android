/*
 * Copyright (c) 2022 Proton Technologies AG
 * This file is part of Proton AG and ProtonCore.
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
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.country.presentation.entity.CountryUIModel
import me.proton.core.country.presentation.ui.CountryPickerFragment
import me.proton.core.country.presentation.ui.showCountryPicker
import me.proton.core.payment.domain.entity.Card
import me.proton.core.payment.domain.entity.Currency
import me.proton.core.payment.domain.entity.PaymentType
import me.proton.core.payment.domain.entity.SubscriptionManagement
import me.proton.core.payment.presentation.R
import me.proton.core.payment.presentation.databinding.FragmentBillingBinding
import me.proton.core.payment.presentation.entity.BillingInput
import me.proton.core.payment.presentation.entity.PlanShortDetails
import me.proton.core.payment.presentation.viewmodel.BillingCommonViewModel.Companion.buildPlansList
import me.proton.core.payment.presentation.viewmodel.BillingViewModel
import me.proton.core.presentation.ui.ProtonFragment
import me.proton.core.presentation.ui.view.ProtonInput
import me.proton.core.presentation.utils.addOnBackPressedCallback
import me.proton.core.presentation.utils.formatCentsPriceDefaultLocale
import me.proton.core.presentation.utils.hideKeyboard
import me.proton.core.presentation.utils.onClick
import me.proton.core.presentation.utils.onTextChange
import me.proton.core.presentation.utils.viewBinding
import me.proton.core.util.kotlin.exhaustive

/**
 * Fragment that handles Billing Credit/Debit card input.
 */
@AndroidEntryPoint
internal class BillingFragment : ProtonFragment(R.layout.fragment_billing) {

    private val viewModel: BillingViewModel by viewModels({ requireActivity() })
    private val binding by viewBinding(FragmentBillingBinding::bind)

    private var amount: Long? = null
    private lateinit var currency: Currency

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            cardNumberInput.apply {
                endIconMode = ProtonInput.EndIconMode.CUSTOM_ICON
                endIconDrawable = ContextCompat.getDrawable(context, R.drawable.ic_proton_credit_card)
                onTextChange(afterTextChangeListener = CardNumberWatcher().watcher)
            }

            expirationDateInput.apply {
                onTextChange(afterTextChangeListener = ExpirationDateWatcher().watcher)
            }

            countriesText.onClick {
                childFragmentManager.showCountryPicker(false)
            }

            childFragmentManager.setFragmentResultListener(
                CountryPickerFragment.KEY_COUNTRY_SELECTED, this@BillingFragment
            ) { _, bundle ->
                val country = bundle.getParcelable<CountryUIModel>(CountryPickerFragment.BUNDLE_KEY_COUNTRY)
                countriesText.text = country?.name
            }
        }

        viewModel.userInteractionState
            .onEach {
                when (it) {
                    is BillingViewModel.UserInteractionState.OnLoadingStateChange -> inputState(it.loading)
                    is BillingViewModel.UserInteractionState.OnPay -> onPayClicked(it.input)
                    is BillingViewModel.UserInteractionState.PlanValidated -> setPlan(it.plan)
                    else -> {
                        // no operation, not interested in other events
                    }
                }.exhaustive
            }.launchIn(lifecycleScope)

        addOnBackPressedCallback {
            requireActivity().finish()
        }
    }

    override fun onResume() {
        super.onResume()
        updatePayButtonText()
    }

    private fun setPlan(plan: PlanShortDetails) {
        binding.selectedPlanDetailsLayout.plan = plan
        amount = plan.amount
        currency = plan.currency
        updatePayButtonText()
    }

    private fun updatePayButtonText() {
        viewModel.setPayButtonStateEnabled(
            String.format(
                getString(R.string.payments_pay),
                amount?.toDouble()?.formatCentsPriceDefaultLocale(currency.name) ?: ""
            )
        )
    }

    private fun onPayClicked(input: BillingInput) = with(binding) {
        hideKeyboard()
        val numberOfInvalidFields = billingInputFieldsValidationList(requireContext()).filter {
            !it.isValid
        }.size

        if (numberOfInvalidFields > 0) {
            viewModel.setPayButtonsState(false)
            return@with
        }

        val expirationDate = expirationDateInput.text.toString().split(BillingActivity.EXP_DATE_SEPARATOR)

        viewModel.subscribe(
            input.user,
            input.existingPlans.buildPlansList(input.plan.name, input.plan.services, input.plan.type),
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
            ),
            SubscriptionManagement.PROTON_MANAGED
        )
    }

    private fun inputState(loading: Boolean) {
        val enabled = !loading
        with(binding) {
            cardNameInput.isEnabled = enabled
            cardNumberInput.isEnabled = enabled
            expirationDateInput.isEnabled = enabled
            cvcInput.isEnabled = enabled
            postalCodeInput.isEnabled = enabled
            countriesText.isEnabled = enabled
        }
    }
}
