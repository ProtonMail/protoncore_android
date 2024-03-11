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

package me.proton.core.paymentiap.presentation.usecase

import kotlinx.coroutines.flow.first
import me.proton.core.domain.entity.UserId
import me.proton.core.humanverification.domain.HumanVerificationManager
import me.proton.core.network.domain.client.ClientIdProvider
import me.proton.core.payment.domain.entity.Currency
import me.proton.core.payment.domain.entity.GooglePurchase
import me.proton.core.payment.domain.entity.PaymentTokenStatus
import me.proton.core.payment.domain.entity.PaymentType
import me.proton.core.payment.domain.entity.ProductId
import me.proton.core.payment.domain.entity.SubscriptionCycle
import me.proton.core.payment.domain.usecase.CreatePaymentToken
import me.proton.core.payment.presentation.entity.BillingResult
import me.proton.core.plan.domain.entity.DynamicPlan
import me.proton.core.plan.domain.usecase.CreatePaymentTokenForGooglePurchase
import me.proton.core.plan.domain.usecase.ObserveUserCurrency
import me.proton.core.plan.domain.usecase.ValidateSubscriptionPlan
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.extension.isNullOrCredentialLess
import javax.inject.Inject

/**
 * Creates a Proton subscription after a successful GIAP payment.
 * - validate subscription
 * - create payment token from Google purchase token
 * - add HV data (if no userId)
 */
public class CreatePaymentTokenForGooglePurchaseImpl @Inject constructor(
    private val clientIdProvider: ClientIdProvider,
    private val createPaymentToken: CreatePaymentToken,
    private val humanVerificationManager: HumanVerificationManager,
    private val observeUserCurrency: ObserveUserCurrency,
    private val userManager: UserManager,
    private val validateSubscriptionPlan: ValidateSubscriptionPlan
) : CreatePaymentTokenForGooglePurchase {
    override suspend fun invoke(
        cycle: Int,
        googleProductId: ProductId,
        plan: DynamicPlan,
        purchase: GooglePurchase,
        userId: UserId?
    ): CreatePaymentTokenForGooglePurchase.Result {
        require(purchase.productIds.contains(googleProductId)) { "Missing product in Google purchase." }
        val planName = requireNotNull(plan.name) { "Missing plan name for plan ${plan.title}." }
        val planNames = listOf(planName)
        val currency = observeUserCurrency(userId).first()

        val subscriptionStatus = validateSubscriptionPlan(
            userId,
            codes = null,
            plans = planNames,
            currency = Currency.valueOf(currency),
            cycle = SubscriptionCycle.map[cycle] ?: SubscriptionCycle.OTHER
        )

        val tokenResult = createPaymentToken(
            userId = userId,
            amount = subscriptionStatus.amountDue,
            currency = subscriptionStatus.currency,
            paymentType = PaymentType.GoogleIAP(
                productId = googleProductId.id,
                purchaseToken = purchase.purchaseToken,
                orderId = requireNotNull(purchase.orderId),
                packageName = purchase.packageName,
                customerId = requireNotNull(purchase.customerId)
            )
        )

        check(tokenResult.status == PaymentTokenStatus.CHARGEABLE) {
            "Unexpected status for creating payment token: ${tokenResult.status}."
        }

        if (userId.isNullOrCredentialLess(userManager)) {
            val clientId = requireNotNull(clientIdProvider.getClientId(sessionId = null))
            humanVerificationManager.addDetails(
                BillingResult.paymentDetails(
                    clientId = clientId,
                    token = tokenResult.token
                )
            )
        }

        return CreatePaymentTokenForGooglePurchase.Result(
            amount = subscriptionStatus.amountDue,
            cycle = subscriptionStatus.cycle,
            currency = subscriptionStatus.currency,
            planNames = planNames,
            token = tokenResult.token,
        )
    }
}
