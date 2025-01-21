/*
 * Copyright (c) 2023 Proton AG
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

package me.proton.core.plan.data.repository

import io.github.reactivecircus.cache4k.Cache
import me.proton.core.domain.entity.AppStore
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.domain.onParseErrorLog
import me.proton.core.payment.domain.features.IsPaymentsV5Enabled
import me.proton.core.payment.domain.entity.Currency
import me.proton.core.payment.domain.entity.PaymentTokenEntity
import me.proton.core.payment.domain.entity.SubscriptionCycle
import me.proton.core.payment.domain.entity.SubscriptionStatus
import me.proton.core.payment.domain.repository.PlanQuantity
import me.proton.core.plan.data.api.PlansApi
import me.proton.core.plan.data.api.request.CheckSubscription
import me.proton.core.plan.data.api.request.CreateSubscription
import me.proton.core.plan.data.api.response.toDynamicPlan
import me.proton.core.plan.data.usecase.GetSessionUserIdForPaymentApi
import me.proton.core.plan.domain.LogTag
import me.proton.core.plan.domain.PlanIconsEndpointProvider
import me.proton.core.plan.domain.entity.DynamicPlans
import me.proton.core.plan.domain.entity.DynamicSubscription
import me.proton.core.plan.domain.entity.Subscription
import me.proton.core.plan.domain.entity.SubscriptionManagement
import me.proton.core.plan.domain.repository.PlansRepository
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.extension.isNullOrCredentialLess
import me.proton.core.util.kotlin.coroutine.result
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.minutes

@Singleton
class PlansRepositoryImpl @Inject constructor(
    private val apiProvider: ApiProvider,
    private val endpointProvider: PlanIconsEndpointProvider,
    private val getSessionUserIdForPaymentApi: GetSessionUserIdForPaymentApi,
    private val userManager: UserManager,
    private val isPaymentsV5Enabled: IsPaymentsV5Enabled,
) : PlansRepository {

    private val dynamicPlansCache =
        Cache.Builder().expireAfterWrite(1.minutes).build<String, DynamicPlans>()

    private fun clearPlansCache() {
        dynamicPlansCache.invalidateAll()
    }

    private suspend fun getRemoteDynamicPlans(
        sessionUserId: SessionUserId?,
        appStore: AppStore,
    ): DynamicPlans = result("getDynamicPlans") {
        apiProvider.get<PlansApi>(sessionUserId).invoke {
            val response = getDynamicPlans(appStore.value)
            DynamicPlans(
                defaultCycle = response.defaultCycle,
                plans = response.plans.mapIndexed { index, resource ->
                    resource.toDynamicPlan(endpointProvider.get(), index)
                }.sortedBy { plan -> plan.order }
            )
        }.onParseErrorLog(LogTag.DYN_PLANS_PARSE).valueOrThrow
    }

    override suspend fun getDynamicPlans(
        sessionUserId: SessionUserId?,
        appStore: AppStore,
    ): DynamicPlans = dynamicPlansCache.get(sessionUserId?.id ?: "") {
        getRemoteDynamicPlans(sessionUserId, appStore)
    }

    override suspend fun validateSubscription(
        sessionUserId: SessionUserId?,
        codes: List<String>?,
        plans: PlanQuantity,
        currency: Currency,
        cycle: SubscriptionCycle
    ): SubscriptionStatus = result("validateSubscription") {
        apiProvider.get<PlansApi>(getSessionUserIdForPaymentApi(sessionUserId)).invoke {
            val requestBody = CheckSubscription(codes, plans, currency.name, cycle.value)
            if (isPaymentsV5Enabled(sessionUserId)) {
                validateSubscriptionV5(requestBody).toSubscriptionStatus()
            } else {
                validateSubscription(requestBody).toSubscriptionStatus()
            }
        }.valueOrThrow
    }

    override suspend fun getSubscription(sessionUserId: SessionUserId): Subscription? =
        apiProvider.get<PlansApi>(sessionUserId).invoke {
            getCurrentSubscription().subscription.toSubscription()
        }.valueOrThrow

    override suspend fun getDynamicSubscriptions(sessionUserId: SessionUserId): List<DynamicSubscription> =
        if (sessionUserId.isNullOrCredentialLess(userManager)) {
            listOf(DynamicSubscription(name = null, title = "", description = ""))
        } else {
            result("getDynamicSubscriptions") {
                apiProvider.get<PlansApi>(sessionUserId).invoke {
                    getDynamicSubscriptions().subscriptions.map { it.toDynamicSubscription(endpointProvider.get()) }
                }.onParseErrorLog(me.proton.core.payment.domain.LogTag.DYN_SUB_PARSE).valueOrThrow
            }
        }

    override suspend fun createOrUpdateSubscription(
        sessionUserId: SessionUserId,
        amount: Long,
        currency: Currency,
        payment: PaymentTokenEntity?,
        codes: List<String>?,
        plans: PlanQuantity,
        cycle: SubscriptionCycle,
        subscriptionManagement: SubscriptionManagement
    ): Subscription = result("createOrUpdateSubscription") {
        apiProvider.get<PlansApi>(sessionUserId).invoke {
            val requestBody = CreateSubscription(
                amount = amount,
                currency = currency.name,
                paymentToken = payment?.token?.value,
                codes = codes,
                plans = plans,
                cycle = cycle.value,
                external = subscriptionManagement.value
            )
            if (isPaymentsV5Enabled(sessionUserId)) {
                createUpdateSubscriptionV5(body = requestBody).subscription.toSubscription()
            } else {
                createUpdateSubscription(body = requestBody).subscription.toSubscription()
            }
        }.valueOrThrow.apply {
            clearPlansCache()
        }
    }
}
