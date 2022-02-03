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

package me.proton.core.plan.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.domain.entity.UserId
import me.proton.core.payment.domain.usecase.GetCurrentSubscription
import me.proton.core.payment.presentation.PaymentsOrchestrator
import me.proton.core.plan.domain.SupportedUpgradePaidPlans
import me.proton.core.plan.domain.usecase.GetPlans
import me.proton.core.plan.presentation.entity.PlanCurrency
import me.proton.core.plan.presentation.entity.PlanDetailsListItem
import me.proton.core.plan.presentation.entity.PlanPricing
import me.proton.core.plan.presentation.entity.PlanType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
internal class UpgradePlansViewModel @Inject constructor(
    private val getPlans: GetPlans,
    private val getCurrentSubscription: GetCurrentSubscription,
    @SupportedUpgradePaidPlans val supportedPaidPlanNames: List<String>,
    paymentsOrchestrator: PaymentsOrchestrator
) : BasePlansViewModel(paymentsOrchestrator) {

    private val _subscribedPlansState = MutableStateFlow<SubscribedPlansState>(SubscribedPlansState.Idle)

    val subscribedPlansState = _subscribedPlansState.asStateFlow()

    private lateinit var subscribedPlans: List<PlanDetailsListItem>

    sealed class SubscribedPlansState {
        object Idle : SubscribedPlansState()
        object Processing : SubscribedPlansState()

        sealed class Success : SubscribedPlansState() {
            data class SubscribedPlans(
                val subscribedPlans: List<PlanDetailsListItem>
            ) : Success()
        }

        data class Error(val error: Throwable) : SubscribedPlansState()
    }

    fun getCurrentSubscribedPlans(userId: UserId) = flow {
        emit(SubscribedPlansState.Processing)
        val currentSubscription = getCurrentSubscription(userId)
        val subscribedPlans: MutableList<PlanDetailsListItem> = currentSubscription?.plans?.filter {
            it.type == PlanType.NORMAL.value
        }?.map {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = currentSubscription.periodEnd * 1000
            PlanDetailsListItem.PaidPlanDetailsListItem(
                name = it.name,
                displayName = it.title,
                price = PlanPricing.fromPlan(it),
                selectable = false,
                currentlySubscribed = true,
                upgrade = false,
                renewalDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(calendar.time),
                storage = it.maxSpace,
                members = it.maxMembers,
                addresses = it.maxAddresses,
                calendars = it.maxCalendars,
                domains = it.maxDomains,
                connections = it.maxVPN,
                currency = PlanCurrency.valueOf(it.currency)
            )
        }?.toMutableList() ?: mutableListOf()
        if (subscribedPlans.isEmpty()) {
            subscribedPlans.add(createFreePlan(currentlySubscribed = true, selectable = false))
        }

        this@UpgradePlansViewModel.subscribedPlans = subscribedPlans
        getAvailablePlansForUpgrade(userId)
        emit(SubscribedPlansState.Success.SubscribedPlans(subscribedPlans))
    }.catch { error ->
        _subscribedPlansState.tryEmit(SubscribedPlansState.Error(error))
    }.onEach {
        _subscribedPlansState.tryEmit(it)
    }.launchIn(viewModelScope)

    private fun getAvailablePlansForUpgrade(userId: UserId) = flow {
        emit(PlanState.Processing)
        val availablePlans = getPlans(supportedPaidPlans = supportedPaidPlanNames.map { it }, userId = userId)
            .filter { availablePlan -> subscribedPlans.none { it.name == availablePlan.name } }
            .map { it.toPaidPlanDetailsItem(subscribedPlans, true) }

        emit(PlanState.Success.Plans(plans = availablePlans))
    }.catch { error ->
        _availablePlansState.tryEmit(PlanState.Error(error))
    }.onEach { plans ->
        _availablePlansState.tryEmit(plans)
    }.launchIn(viewModelScope)
}
