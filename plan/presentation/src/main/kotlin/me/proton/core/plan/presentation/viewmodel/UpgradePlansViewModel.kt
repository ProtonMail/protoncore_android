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
import me.proton.core.payment.domain.entity.SubscriptionManagement
import me.proton.core.payment.domain.usecase.GetAvailablePaymentMethods
import me.proton.core.payment.domain.usecase.GetAvailablePaymentProviders
import me.proton.core.payment.domain.usecase.GetCurrentSubscription
import me.proton.core.payment.presentation.PaymentsOrchestrator
import me.proton.core.plan.domain.SupportUpgradePaidPlans
import me.proton.core.plan.domain.usecase.GetPlanDefault
import me.proton.core.plan.domain.usecase.GetPlans
import me.proton.core.plan.presentation.entity.PlanCurrency
import me.proton.core.plan.presentation.entity.PlanDetailsItem
import me.proton.core.plan.presentation.entity.PlanType
import me.proton.core.plan.presentation.entity.UnredeemedGooglePurchase
import me.proton.core.plan.presentation.usecase.CheckUnredeemedGooglePurchase
import me.proton.core.user.domain.usecase.GetUser
import me.proton.core.usersettings.domain.usecase.GetOrganization
import java.util.Calendar
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
internal class UpgradePlansViewModel @Inject @Suppress("LongParameterList") constructor(
    private val checkUnredeemedGooglePurchase: CheckUnredeemedGooglePurchase,
    private val getAvailablePaymentProviders: GetAvailablePaymentProviders,
    private val getPlans: GetPlans,
    private val getPlanDefault: GetPlanDefault,
    private val getCurrentSubscription: GetCurrentSubscription,
    private val getOrganization: GetOrganization,
    private val getUser: GetUser,
    private val getPaymentMethods: GetAvailablePaymentMethods,
    @SupportUpgradePaidPlans val supportPaidPlans: Boolean,
    paymentsOrchestrator: PaymentsOrchestrator
) : BasePlansViewModel(paymentsOrchestrator) {

    private val _subscribedPlansState = MutableStateFlow<SubscribedPlansState>(SubscribedPlansState.Idle)

    val subscribedPlansState = _subscribedPlansState.asStateFlow()

    private lateinit var subscribedPlans: List<PlanDetailsItem>

    sealed class SubscribedPlansState {
        object Idle : SubscribedPlansState()
        object Processing : SubscribedPlansState()

        sealed class Success : SubscribedPlansState() {
            data class SubscribedPlans(
                val subscribedPlans: List<PlanDetailsItem>,
                val renewAmount: Long?,
                val userCurrency: PlanCurrency?,
                val subscriptionManagement: SubscriptionManagement? = null,
                val unredeemedGooglePurchase: UnredeemedGooglePurchase? = null
            ) : Success()
        }

        data class Error(val error: Throwable) : SubscribedPlansState()
    }

    fun getCurrentSubscribedPlans(userId: UserId, checkForUnredeemedPurchase: Boolean = true) = flow {
        emit(SubscribedPlansState.Processing)
        val currentSubscription = getCurrentSubscription(userId)
        val organization = getOrganization(userId, refresh = true)
        val user = getUser(userId, refresh = false)
        val paymentMethods = getPaymentMethods(userId)

        val subscribedPlans: MutableList<PlanDetailsItem> = currentSubscription?.plans?.filter {
            it.type == PlanType.NORMAL.value
        }?.map {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = currentSubscription.periodEnd.seconds.inWholeMilliseconds
            createCurrentPlan(
                plan = it,
                endDate = calendar.time,
                user = user,
                paymentMethods = paymentMethods,
                organization = organization
            )
        }?.toMutableList() ?: mutableListOf()

        val isFree = subscribedPlans.isEmpty()
        if (isFree) {
            subscribedPlans.add(
                createCurrentPlan(
                    plan = getPlanDefault(userId),
                    endDate = null,
                    user = user,
                    paymentMethods = paymentMethods,
                    organization = organization
                ) // creates free plan
            )
        }

        this@UpgradePlansViewModel.subscribedPlans = subscribedPlans
        getAvailablePlansForUpgrade(userId, isFree)
        val external = currentSubscription?.external
        val unredeemed = if (checkForUnredeemedPurchase) {
            checkUnredeemedGooglePurchase(userId)
        } else null
        emit(
            SubscribedPlansState.Success.SubscribedPlans(
                subscribedPlans = subscribedPlans,
                renewAmount = currentSubscription?.renewAmount,
                userCurrency = PlanCurrency.map[user.currency],
                subscriptionManagement = external,
                unredeemedGooglePurchase = unredeemed
            )
        )
    }.catch { error ->
        _subscribedPlansState.tryEmit(SubscribedPlansState.Error(error))
    }.onEach {
        _subscribedPlansState.tryEmit(it)
    }.launchIn(viewModelScope)

    private fun getAvailablePlansForUpgrade(userId: UserId, isFreeUser: Boolean) = flow {
        emit(PlanState.Processing)

        val paymentProviders = getAvailablePaymentProviders()
        val anyPaymentEnabled = paymentProviders.isNotEmpty()
        val availablePlans = when {
            !supportPaidPlans -> emptyList()
            !anyPaymentEnabled -> emptyList()
            !isFreeUser -> emptyList()
            else -> getPlans(userId = userId)
                .filter { availablePlan -> subscribedPlans.none { it.name == availablePlan.name } }
                .map { plan -> plan.toPaidPlanDetailsItem(starred = false) }
        }

        emit(PlanState.Success.Plans(plans = availablePlans, purchaseEnabled = anyPaymentEnabled))
    }.catch { error ->
        state.tryEmit(PlanState.Error.Exception(error))
    }.onEach { plans ->
        state.tryEmit(plans)
    }.launchIn(viewModelScope)
}
