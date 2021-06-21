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
import me.proton.core.plan.domain.SupportedPaidPlanIds
import me.proton.core.plan.domain.entity.Plan
import me.proton.core.plan.domain.usecase.GetPlans
import me.proton.core.plan.presentation.entity.PlanDetailsListItem
import me.proton.core.plan.presentation.entity.PlanPricing
import me.proton.core.plan.presentation.entity.PlanSubscription
import me.proton.core.plan.presentation.entity.PlanType
import me.proton.core.presentation.viewmodel.ProtonViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class PlansViewModel @Inject constructor(
    private val getPlansUseCase: GetPlans,
    private val getCurrentSubscription: GetCurrentSubscription,
    @SupportedPaidPlanIds private val supportedPaidPlanIds: List<String>
) : ProtonViewModel() {

    private val _availablePlansState = MutableStateFlow<State>(State.Idle)

    val availablePlansState = _availablePlansState.asStateFlow()

    sealed class State {
        object Idle : State()
        object Processing : State()
        data class Success(
            val plans: List<PlanDetailsListItem>,
            val subscription: PlanSubscription? = null
        ) : State()

        sealed class Error : State() {
            data class Message(val message: String?) : Error()
        }
    }

    fun getCurrentPlanWithUpgradeOption(userId: UserId? = null, showFreeIfCurrent: Boolean = true) = flow {
        emit(State.Processing)
        val upgrade: Boolean = userId != null
        val currentSubscription = if (userId != null) {
            getCurrentSubscription(userId)
        } else null

        val subscribedPlans: MutableList<PlanDetailsListItem> = currentSubscription?.plans?.filter {
            it.type == PlanType.NORMAL.value
        }?.map {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = currentSubscription.periodEnd * 1000
            PlanDetailsListItem.PaidPlanDetailsListItem(
                id = it.id,
                name = it.name,
                price = PlanPricing.fromPlan(it),
                selectable = false,
                current = true,
                upgrade = upgrade,
                renewalDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(calendar.time)
            )
        }?.toMutableList() ?: mutableListOf()

        val plans: MutableList<PlanDetailsListItem> = if (subscribedPlans.isEmpty() && showFreeIfCurrent) {
            // this means that users current plan is free
            mutableListOf(createFreePlanAsCurrent(current = upgrade, selectable = !upgrade))
        } else subscribedPlans

        if (subscribedPlans.isEmpty()) {
            plans.addAll(getPlansUseCase(supportedPaidPlanIds = supportedPaidPlanIds + subscribedPlans.map {
                it.id
            }, userId = userId)
                .map { it.toPaidPlanDetailsItem(subscribedPlans, upgrade) }
            )
        }

        val planSubscription = PlanSubscription(currentSubscription,
            subscribedPlans.isEmpty() || subscribedPlans.find {
                supportedPaidPlanIds.contains(it.id)
            } != null)
        emit(State.Success(plans = plans, subscription = planSubscription))
    }.catch { error ->
        _availablePlansState.tryEmit(State.Error.Message(error.message))
    }.onEach { plans ->
        _availablePlansState.tryEmit(plans)
    }.launchIn(viewModelScope)

    private fun createFreePlanAsCurrent(current: Boolean, selectable: Boolean): PlanDetailsListItem {
        return PlanDetailsListItem.FreePlanDetailsListItem(
            id = FREE_PLAN_ID,
            current = current,
            selectable = selectable
        )
    }

    companion object {
        const val FREE_PLAN_ID = "free"
    }
}

fun Plan.toPaidPlanDetailsItem(subscribedPlans: MutableList<PlanDetailsListItem>, upgrade: Boolean) =
    PlanDetailsListItem.PaidPlanDetailsListItem(
        id = id,
        name = name,
        price = PlanPricing.fromPlan(this),
        selectable = subscribedPlans.isNullOrEmpty(),
        current = subscribedPlans.find { currentPlan ->
            currentPlan.id == id
        } != null,
        upgrade = upgrade,
        renewalDate = null
    )
