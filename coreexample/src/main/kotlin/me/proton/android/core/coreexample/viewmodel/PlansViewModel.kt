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

package me.proton.android.core.coreexample.viewmodel

import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.proton.android.core.coreexample.PLAN_PLUS_ID
import me.proton.android.core.coreexample.PLAN_VISIONARY_ID
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.payment.presentation.PaymentsOrchestrator
import me.proton.core.payment.presentation.onPaymentResult
import me.proton.core.payment.domain.PaymentManager
import me.proton.core.payment.domain.entity.SubscriptionCycle
import me.proton.core.payment.presentation.entity.PlanShortDetails
import me.proton.core.plan.domain.entity.MASK_MAIL
import me.proton.core.plan.domain.entity.MASK_VPN
import me.proton.core.plan.domain.entity.PLAN_PRODUCT
import me.proton.core.plan.presentation.PlansOrchestrator
import me.proton.core.plan.presentation.onUpgradeResult
import me.proton.core.presentation.utils.showToast
import javax.inject.Inject

@HiltViewModel
class PlansViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private val paymentManager: PaymentManager,
    private val plansOrchestrator: PlansOrchestrator,
    private val paymentsOrchestrator: PaymentsOrchestrator
) : ViewModel() {

    data class State(
        val isUpgradeAvailable: Boolean = false
    )

    private val mutableState = MutableStateFlow(State())
    val state = mutableState.asStateFlow()

    init {
        accountManager.getPrimaryUserId()
            .onEach { mutableState.emit(State(paymentManager.isUpgradeAvailable())) }
            .launchIn(viewModelScope)
    }

    fun register(context: ComponentActivity) {
        plansOrchestrator.register(context)
        paymentsOrchestrator.register(context)
    }

    private fun getPrimaryUserId() = accountManager.getPrimaryUserId()

    fun onPlansClicked() {
        viewModelScope.launch {
            plansOrchestrator.startSignUpPlanChooserWorkflow()
        }
    }

    fun onPlansUpgradeClicked(context: ComponentActivity) {
        viewModelScope.launch {
            getPrimaryUserId().first()?.let {
                val account = accountManager.getAccount(it).first() ?: return@launch
                with(plansOrchestrator) {
                    onUpgradeResult { upgradeResult ->
                        // do something with the upgrade result
                        if (upgradeResult != null) {
                            context.showToast(
                                "Upgrade result: ${upgradeResult.billingResult.token} " +
                                    "for plan id: ${upgradeResult.planId}"
                            )
                        }
                    }

                    plansOrchestrator.startUpgradeWorkflow(account.userId)
                }
            }
        }
    }

    fun onCurrentPlanClicked(context: ComponentActivity) {
        viewModelScope.launch {
            getPrimaryUserId().first()?.let {
                val account = accountManager.getAccount(it).first() ?: return@launch
                with(plansOrchestrator) {
                    onUpgradeResult { upgradeResult ->
                        // do something with the upgrade result
                        if (upgradeResult != null) {
                            context.showToast(
                                "Upgrade result: ${upgradeResult.billingResult.token} " +
                                    "for planId: ${upgradeResult.planId}"
                            )
                        }
                    }

                    plansOrchestrator.showCurrentPlanWorkflow(account.userId)
                }
            }
        }
    }

    fun onPaySignUpClicked() {
        viewModelScope.launch {
            paymentsOrchestrator.startBillingWorkFlow(
                selectedPlan = PlanShortDetails(
                    name = PLAN_PLUS_ID,
                    displayName = "Proton Plus",
                    subscriptionCycle = SubscriptionCycle.YEARLY,
                    services = MASK_MAIL,
                    type = PLAN_PRODUCT,
                    vendorNames = emptyMap()
                )
            )
        }
    }

    fun onPayUpgradeClicked() {
        viewModelScope.launch {
            getPrimaryUserId().first()?.let {
                val account = accountManager.getAccount(it).first() ?: return@launch
                with(paymentsOrchestrator) {
                    onPaymentResult {
                        // do something with the payment result
                    }

                    startBillingWorkFlow(
                        userId = account.userId,
                        selectedPlan = PlanShortDetails(
                            name = PLAN_VISIONARY_ID,
                            displayName = "Proton Visionary",
                            subscriptionCycle = SubscriptionCycle.YEARLY,
                            services = MASK_MAIL + MASK_VPN,
                            type = PLAN_PRODUCT,
                            vendorNames = emptyMap()
                        ),
                        codes = null
                    )
                }
            }
        }
    }
}
