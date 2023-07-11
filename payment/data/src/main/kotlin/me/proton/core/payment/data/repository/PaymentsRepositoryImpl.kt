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

package me.proton.core.payment.data.repository

import me.proton.core.domain.entity.AppStore
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.network.data.ApiProvider
import me.proton.core.payment.data.api.PaymentsApi
import me.proton.core.payment.data.api.request.CardDetailsBody
import me.proton.core.payment.data.api.request.CheckSubscription
import me.proton.core.payment.data.api.request.CreatePaymentToken
import me.proton.core.payment.data.api.request.CreateSubscription
import me.proton.core.payment.data.api.request.IAPDetailsBody
import me.proton.core.payment.data.api.request.PaymentTypeEntity
import me.proton.core.payment.domain.entity.Card
import me.proton.core.payment.domain.entity.Currency
import me.proton.core.payment.domain.entity.PaymentMethod
import me.proton.core.payment.domain.entity.PaymentMethodType
import me.proton.core.payment.domain.entity.PaymentStatus
import me.proton.core.payment.domain.entity.PaymentTokenEntity
import me.proton.core.payment.domain.entity.PaymentTokenResult
import me.proton.core.payment.domain.entity.PaymentType
import me.proton.core.payment.domain.entity.ProtonPaymentToken
import me.proton.core.payment.domain.entity.Subscription
import me.proton.core.payment.domain.entity.SubscriptionCycle
import me.proton.core.payment.domain.entity.SubscriptionManagement
import me.proton.core.payment.domain.entity.SubscriptionStatus
import me.proton.core.payment.domain.repository.PaymentsRepository
import me.proton.core.payment.domain.repository.PlanQuantity
import me.proton.core.util.kotlin.coroutine.result
import javax.inject.Inject

public class PaymentsRepositoryImpl @Inject constructor(
    private val provider: ApiProvider
) : PaymentsRepository {

    override suspend fun createPaymentToken(
        sessionUserId: SessionUserId?,
        amount: Long,
        currency: Currency,
        paymentType: PaymentType
    ): PaymentTokenResult.CreatePaymentTokenResult = result("createPaymentToken") {
        val request = when (paymentType) {
            is PaymentType.PayPal -> CreatePaymentToken(
                amount = amount,
                currency = currency.name,
                paymentEntity = PaymentTypeEntity.PayPal,
                paymentMethodId = null
            )

            is PaymentType.CreditCard -> when (paymentType.card) {
                is Card.CardReadOnly -> throw IllegalArgumentException("Insufficient Payment Details provided.")
                is Card.CardWithPaymentDetails -> CreatePaymentToken(
                    amount = amount,
                    currency = currency.name,
                    paymentEntity = PaymentTypeEntity.Card(
                        CardDetailsBody(
                            number = (paymentType.card as Card.CardWithPaymentDetails).number,
                            cvc = (paymentType.card as Card.CardWithPaymentDetails).cvc,
                            expirationMonth = paymentType.card.expirationMonth,
                            expirationYear = paymentType.card.expirationYear,
                            name = paymentType.card.name,
                            country = paymentType.card.country,
                            zip = paymentType.card.zip
                        )
                    ),
                    paymentMethodId = null
                )
            }

            is PaymentType.GoogleIAP -> CreatePaymentToken(
                amount = amount,
                currency = currency.name,
                paymentEntity = PaymentTypeEntity.GoogleIAP(
                    IAPDetailsBody(
                        productId = paymentType.productId,
                        purchaseToken = paymentType.purchaseToken.value,
                        orderId = paymentType.orderId,
                        packageName = paymentType.packageName,
                        customerId = paymentType.customerId
                    )
                ),
                paymentMethodId = null
            )

            is PaymentType.PaymentMethod -> CreatePaymentToken(
                amount = amount,
                currency = currency.name,
                paymentEntity = null,
                paymentMethodId = paymentType.paymentMethodId
            )
        }
        provider.get<PaymentsApi>(sessionUserId).invoke {
            createPaymentToken(request).toCreatePaymentTokenResult()
        }.valueOrThrow
    }

    override suspend fun getPaymentTokenStatus(
        sessionUserId: SessionUserId?,
        paymentToken: ProtonPaymentToken
    ): PaymentTokenResult.PaymentTokenStatusResult =
        provider.get<PaymentsApi>(sessionUserId).invoke {
            getPaymentTokenStatus(paymentToken.value).toPaymentTokenStatusResult()
        }.valueOrThrow

    override suspend fun getAvailablePaymentMethods(
        sessionUserId: SessionUserId
    ): List<PaymentMethod> = result("getAvailablePaymentMethods") {
        provider.get<PaymentsApi>(sessionUserId).invoke {
            getPaymentMethods().paymentMethods.map {
                PaymentMethod(it.id, PaymentMethodType.map[it.type] ?: PaymentMethodType.CARD, it.toDetails())
            }
        }.valueOrThrow
    }

    override suspend fun validateSubscription(
        sessionUserId: SessionUserId?,
        codes: List<String>?,
        plans: PlanQuantity,
        currency: Currency,
        cycle: SubscriptionCycle
    ): SubscriptionStatus = result("validateSubscription") {
        provider.get<PaymentsApi>(sessionUserId).invoke {
            validateSubscription(
                CheckSubscription(codes, plans, currency.name, cycle.value)
            ).toSubscriptionStatus()
        }.valueOrThrow
    }

    override suspend fun getSubscription(sessionUserId: SessionUserId): Subscription? =
        provider.get<PaymentsApi>(sessionUserId).invoke {
            getCurrentSubscription().subscription.toSubscription()
        }.valueOrThrow

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
        provider.get<PaymentsApi>(sessionUserId).invoke {
            createUpdateSubscription(
                body = CreateSubscription(
                    amount = amount,
                    currency = currency.name,
                    paymentToken = payment?.token?.value,
                    codes = codes,
                    plans = plans,
                    cycle = cycle.value,
                    external = subscriptionManagement.value
                )
            ).subscription.toSubscription()
        }.valueOrThrow
    }

    override suspend fun getPaymentStatus(sessionUserId: SessionUserId?, appStore: AppStore): PaymentStatus {
        val appStoreCode = when (appStore) {
            AppStore.FDroid -> "fdroid"
            AppStore.GooglePlay -> "google"
        }
        return provider.get<PaymentsApi>(sessionUserId).invoke {
            paymentStatus(appStoreCode).toPaymentStatus()
        }.valueOrThrow
    }
}
